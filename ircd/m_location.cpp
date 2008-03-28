/*       +------------------------------------+
 *       | Inspire Internet Relay Chat Daemon |
 *       +------------------------------------+
 *
 *  InspIRCd: (C) 2002-2008 InspIRCd Development Team
 * See: http://www.inspircd.org/wiki/index.php/Credits
 *
 * This program is free but copyrighted software; see
 *            the file COPYING for details.
 *
 * ---------------------------------------------------
 *
 * m_location - add location awareness to inspircd
 * author: jeff katz (kraln@kraln.com)
 *
 * written for the google android challenge as a part of androidchat
 *
 * ---------------------------------------------------
 */

#include "inspircd.h"

/* $ModDesc: Provides the location awareness for users and channels*/

namespace mlocpriv {

InspIRCd* mlocservinst;
inline std::string stringify(float x) {
	std::ostringstream o;
	if (!(o << x))
		;
	return o.str();
}

void updateChanLocation(Channel* channel) {
	// check for presense of channel fixed location mode
	// if not present, set channel location to average of users location

	float clat, clng;
	clat = 0;
	clng = 0;
	signed int size = 0;
	std::string* lat;
	std::string* lng;

	if (channel->IsModeSet('L')) {
		// don't uh do anything. I don't think
		mlocservinst->Logs->Log("M_LOCATION", DEBUG, "-Mode +L was set!!!");
		return;
	} else {
		// update location based on users
		CUList* ulist = channel->GetUsers();
		mlocservinst->Logs->Log("M_LOCATION", DEBUG,
				"-Recalculating channel location");
		for (CUListIter i = ulist->begin(); i != ulist->end(); i++) {
			User* what = i->first;

			float flat = 0.0f;
			float flng = 0.0f;

			what->GetExt("location_lat", lat);
			what->GetExt("location_lng", lng);

			if (lng) {
				if (lat) {
					sscanf(lat->c_str(), "%f", &flat);
					sscanf(lng->c_str(), "%f", &flng);

					mlocservinst->Logs->Log(
							"M_LOCATION",
							DEBUG,
							"---User has location (%f, %f), based on sscanf of (%s, %s)!",
							flat, flng, lat->c_str(), lng->c_str());

					if (flat == 0.0f) {
						if (flng == 0.0f) // sorry to users off of the west coast of africa
						{
							mlocservinst->Logs->Log("M_LOCATION", DEBUG,
									"---User location invalid, removing from count...");
							size--; // take this one out
							continue;
						}
					} else {
						mlocservinst->Logs->Log("M_LOCATION", DEBUG,
								"---User added to count...");
						size++; // add this one in
						clat += flat;
						clng += flng;

					}
				}
			} else {
				size--;
				mlocservinst->Logs->Log(
						"M_LOCATION",
						DEBUG,
						"---User has no location, removing from count... (new count: %d)",
						size);

				continue;
			}
		}
	}

	float mul = (1.0f)/(float)size;
	mlocservinst->Logs->Log(
			"M_LOCATION",
			DEBUG,
			"-Adjusting channel location. Params: (%f, %f), factor of 1/%i (%f)",
			clat, clng, size, mul);

	if (size<1) {
		mlocservinst->Logs->Log("M_LOCATION", DEBUG,
				"-Not adjusting channel location (no valid users)");
		return;
	}

	clat *= mul;
	clng *= mul;

	channel->GetExt("location_lat", lat);
	channel->GetExt("location_lng", lng);

	if (lat) // if already set
	{
		channel->Shrink("location_lat");
		delete lat;
	}
	if (lng) {
		channel->Shrink("location_lng");
		delete lng;
	}

	lat = new std::string(stringify(clat));
	lng = new std::string(stringify(clng));

	mlocservinst->Logs->Log("M_LOCATION", DEBUG,
			"Adjusting channel location. Stringified Params: (%s, %s)",
			lat->c_str(), lng->c_str());

	channel->Extend("location_lat", lat);
	channel->Extend("location_lng", lng);

}

void sendNumericForChannel(Channel* channel, User* user) {
	// send user numeric
	// will be called onjoin, and when a user updates their location
	// :irc.androidchat.net 641 #channelname -34.155233523 -13.235253
	std::string* lat;
	std::string* lng;
	channel->GetExt("location_lat", lat);
	channel->GetExt("location_lng", lng);

	if (lng && lat)
			user->WriteServ("641 %s %s %s %s", user->nick, channel->name, lat->c_str(), lng->c_str());
	else
		user->WriteServ("641 %s %s -0 -0", user->nick, channel->name);


}

void sendNumericForAllChannel(Channel* channel) {
	// for each user on the channel, send them channel location
	// will be called when a user updates their location
	for (CUListIter i = channel->GetUsers()->begin(); i != channel->GetUsers()->end(); i++) {
		User* what = i->first;
		sendNumericForChannel(channel, what); // update each of the channel's users
	}

}

void updateChansUserIsIn(User* user) {
	// chans is typedef std::map<Channel*, char> UserChanList;
	// i-> first is the channel half (the part we care about)
	for (UCListIter i = user->chans.begin(); i != user->chans.end(); i++) {
		Channel* what = i->first;
		updateChanLocation(what); // update the channel's location
		sendNumericForAllChannel(what); // update each of the channel's users
	}

}

}

/** Handles channel mode +L
 */
class ChannelLocation : public ModeHandler {
public:
	ChannelLocation(InspIRCd* Instance) :
		ModeHandler(Instance, 'L', 0, 0, false, MODETYPE_CHANNEL, false) {
	}

	ModeAction OnModeChange(User* source, User* dest, Channel* channel,
			std::string &parameter, bool adding, bool) {
		if (adding) {
			if (!channel->IsModeSet('L')) {
				channel->SetMode('L', true);
				return MODEACTION_ALLOW;
			}
		} else {
			if (channel->IsModeSet('L')) {
				channel->SetMode('L', false);
				return MODEACTION_ALLOW;
			}
		}

		return MODEACTION_DENY;
	}
};

/** Handle /sloc, /scloc
 */

class CommandSLOC : public Command {

public:
	CommandSLOC(InspIRCd* Instance) :
		Command(Instance, "SLOC", "", 2) {
		this->source = "m_location.so";
		syntax = "<lat> <lng>";
		TRANSLATE3(TR_NICK, TR_TEXT, TR_END)
		;
	}

	CmdResult Handle(const char* const* parameters, int pcnt, User* user) {
		User* dest = user;
		if (!dest) {
			user->WriteNumeric(401, "%s %s :No such nick/channel", user->nick,
					parameters[0]);
			return CMD_FAILURE;
		}

		std::string in_lat;
		std::string in_lng;

		in_lat.append(parameters[0]); // should validate input.
		in_lng.append(parameters[1]);

		std::string* lat;
		std::string* lng;

		dest->GetExt("location_lat", lat);
		dest->GetExt("location_lng", lng);

		if (lat) // if already set
		{
			dest->Shrink("location_lat");
			delete lat;
		}
		if (lng) {
			dest->Shrink("location_lng");
			delete lng;
		}

		lat = new std::string(in_lat);
		lng = new std::string(in_lng);

		dest->Extend("location_lat", lat);
		dest->Extend("location_lng", lng);

		std::deque<std::string>* metadata = new std::deque<std::string>;
		metadata->push_back(dest->nick);

		metadata->push_back("location_lat"); // The metadata id
		metadata->push_back(in_lat); // The value to send
		metadata->push_back("location_lng"); // The metadata id
		metadata->push_back(in_lng); // The value to send


		Event event((char*)metadata, (Module*)this, "send_metadata");
		event.Send(ServerInstance);
		delete metadata;

		mlocpriv::updateChansUserIsIn(dest);
		return CMD_LOCALONLY;
	}

};

class CommandSCLOC : public Command {

public:
	CommandSCLOC(InspIRCd* Instance) :
		Command(Instance, "SCLOC", "", 3) {
		this->source = "m_location.so";
		syntax = "<channel> <lat> <lng>";
		TRANSLATE3(TR_NICK, TR_TEXT, TR_END)
		;
	}

	CmdResult Handle(const char* const* parameters, int pcnt, User* user) {
		Channel* dest = ServerInstance->FindChan(parameters[0]);
		if (!dest) {
			user->WriteNumeric(401, "%s %s :No such nick/channel", user->nick,
					parameters[0]);
			return CMD_FAILURE;
		}

		std::string in_lat;
		std::string in_lng;

		in_lat.append(parameters[1]); // should validate input.
		in_lng.append(parameters[2]);

		std::string* lat;
		std::string* lng;

		dest->GetExt("location_lat", lat);
		dest->GetExt("location_lng", lng);

		if (lat) // if already set
		{
			dest->Shrink("location_lat");
			delete lat;
		}
		if (lng) {
			dest->Shrink("location_lng");
			delete lng;
		}

		lat = new std::string(in_lat);
		lng = new std::string(in_lng);

		dest->Extend("location_lat", lat);
		dest->Extend("location_lng", lng);

		std::deque<std::string>* metadata = new std::deque<std::string>;
		metadata->push_back(dest->name);

		metadata->push_back("location_lat"); // The metadata id
		metadata->push_back(in_lat); // The value to send
		metadata->push_back("location_lng"); // The metadata id
		metadata->push_back(in_lng); // The value to send


		Event event((char*)metadata, (Module*)this, "send_metadata");
		event.Send(ServerInstance);
		delete metadata;

		mlocpriv::sendNumericForAllChannel(dest);

		return CMD_LOCALONLY;
	}

};

class CommandGLOC : public Command {

public:
	CommandGLOC(InspIRCd* Instance) :
		Command(Instance, "GLOC", "", 1) {
		this->source = "m_location.so";
		syntax = "<user>";
		TRANSLATE3(TR_NICK, TR_TEXT, TR_END)
		;
	}

	CmdResult Handle(const char* const* parameters, int pcnt, User* user) {
		User* dest = ServerInstance->FindNick(parameters[0]);
		if (!dest) {
			user->WriteNumeric(401, "%s %s :No such nick/channel", user->nick,
					parameters[0]);
			return CMD_FAILURE;
		}

		std::string* lat;
		std::string* lng;

		dest->GetExt("location_lat", lat);
		dest->GetExt("location_lng", lng);

		if(lat && lng)
		ServerInstance->SendWhoisLine(user, dest, 640,
									"%s %s %s %s", user->nick, dest->nick,
									lat->c_str(), lng->c_str());
		else
		ServerInstance->SendWhoisLine(user, dest, 640,
											"%s %s -0 -0", user->nick, dest->nick);
		
		
		return CMD_LOCALONLY;
	}

};

class CommandGCLOC : public Command {

public:
	CommandGCLOC(InspIRCd* Instance) :
		Command(Instance, "GCLOC", "", 1) {
		this->source = "m_location.so";
		syntax = "<channel>";
		TRANSLATE3(TR_NICK, TR_TEXT, TR_END)
		;
	}

	CmdResult Handle(const char* const* parameters, int pcnt, User* user) {
		Channel* dest = ServerInstance->FindChan(parameters[0]);
		if (!dest) {
			user->WriteNumeric(401, "%s %s :No such nick/channel", user->nick,
					parameters[0]);
			return CMD_FAILURE;
		}

		mlocpriv::sendNumericForChannel(dest, user);
		
		return CMD_LOCALONLY;
	}

};

class ModuleSLOC : public Module {
	CommandSLOC* mycommand;
	CommandSCLOC* mycommand2;
	CommandGLOC* mycommand3;
	CommandGCLOC* mycommand4;

	ConfigReader* Conf;
	ChannelLocation* cl;
public:

	ModuleSLOC(InspIRCd* Me) :
		Module(Me) {

		Conf = new ConfigReader(ServerInstance);
		mycommand = new CommandSLOC(ServerInstance);
		mycommand2 = new CommandSCLOC(ServerInstance);
		mycommand3 = new CommandGLOC(ServerInstance);
		mycommand4 = new CommandGCLOC(ServerInstance);
		
		cl = new ChannelLocation(ServerInstance);
		ServerInstance->AddCommand(mycommand);
		ServerInstance->AddCommand(mycommand2);
		ServerInstance->AddCommand(mycommand3);
		ServerInstance->AddCommand(mycommand4);


		if (!ServerInstance->Modes->AddMode(cl)) {
			delete cl;
			throw ModuleException("Could not add new modes!");
		}
		mlocpriv::mlocservinst = ServerInstance;
		Implementation eventlist[] = { I_OnDecodeMetaData, I_OnWhoisLine,
				I_OnSyncUserMetaData, I_OnUserQuit, I_OnCleanup, I_OnRehash,
				I_OnPostJoin, I_OnUserPart };
		ServerInstance->Modules->Attach(eventlist, this, 8);
	}

	void OnRehash(User* user, const std::string &parameter) {
		delete Conf;
		Conf = new ConfigReader(ServerInstance);
	}

	void OnUserPart(User * user, Channel * channel,
			const std::string & partmessage, bool & silent) {
		mlocpriv::updateChanLocation(channel);
		mlocpriv::sendNumericForAllChannel(channel);
	}
	void OnPostJoin(User* user, Channel* channel) {
		mlocpriv::updateChanLocation(channel);
		mlocpriv::sendNumericForAllChannel(channel);
	}

	// :irc.androidchat.net 640 Brain Azhrarn -50.38953232 39.35238523
	int OnWhoisLine(User* user, User* dest, int &numeric, std::string &text) {
		/* We use this and not OnWhois because this triggers for remote, too */
		if (numeric == 312) {
			/* Insert our numeric before 312 */
			std::string* lat;
			std::string* lng;
			dest->GetExt("location_lat", lat);
			dest->GetExt("location_lng", lng);

			if (lng)
				if (lat) {
					ServerInstance->SendWhoisLine(user, dest, 640,
							"%s %s %s %s", user->nick, dest->nick,
							lat->c_str(), lng->c_str());
				}
		}
		/* Dont block anything */
		return 0;
	}

	// Whenever the linking module wants to send out data, but doesnt know what the data
	// represents (e.g. it is metadata, added to a User or Channel by a module) then
	// this method is called. We should use the ProtoSendMetaData function after we've
	// corrected decided how the data should look, to send the metadata on its way if
	// it is ours.
	virtual void OnSyncUserMetaData(User* user, Module* proto, void* opaque,
			const std::string &extname, bool displayable) {
		// check if the linking module wants to know about OUR metadata
		if (extname == "location_lat") {
			// check if this user has an swhois field to send
			std::string* lat;
			user->GetExt("location_lat", lat);
			if (lat)
				proto->ProtoSendMetaData(opaque, TYPE_USER, user, extname, *lat);
		}

		if (extname == "location_lng") {
			// check if this user has an swhois field to send
			std::string* lng;
			user->GetExt("location_lng", lng);
			if (lng)
				proto->ProtoSendMetaData(opaque, TYPE_USER, user, extname, *lng);
		}
	}

	// when a user quits, tidy up their metadata
	virtual void OnUserQuit(User* user, const std::string &message,
			const std::string &oper_message) {
		std::string* loc;
		user->GetExt("location_lng", loc);
		if (loc) {
			user->Shrink("location_lng");
			delete loc;
		}
		user->GetExt("location_lat", loc);
		if (loc) {
			user->Shrink("location_lat");
			delete loc;
		}
	}

	// if the module is unloaded, tidy up all our dangling metadata
	virtual void OnCleanup(int target_type, void* item) {
		if (target_type == TYPE_USER) {
			User* user = (User*)item;
			std::string* loc;
			user->GetExt("location_lat", loc);
			if (loc) {
				user->Shrink("location_lat");
				delete loc;
			}
			user->GetExt("location_lng", loc);
			if (loc) {
				user->Shrink("location_lng");
				delete loc;
			}
		}
	}

	// Whenever the linking module receives metadata from another server and doesnt know what
	// to do with it (of course, hence the 'meta') it calls this method, and it is up to each
	// module in turn to figure out if this metadata key belongs to them, and what they want
	// to do with it.
	// In our case we're only sending a single string around, so we just construct a std::string.
	// Some modules will probably get much more complex and format more detailed structs and classes
	// in a textual way for sending over the link.
	virtual void OnDecodeMetaData(int target_type, void* target,
			const std::string &extname, const std::string &extdata) {
		// check if its our metadata key, and its associated with a user
		if ((target_type == TYPE_USER) && (extname == "location_lat")) {
			User* dest = (User*)target;
			// if they dont already have an swhois field, accept the remote server's
			std::string* loc;
			if (dest->GetExt("location_lat", loc)) {
				dest->Shrink("location_lat");
				delete loc;
			}

			std::string* loc2 = new std::string(extdata);
			dest->Extend("location_lat", loc2);
		}
		if ((target_type == TYPE_USER) && (extname == "location_lng")) {
			User* dest = (User*)target;
			// if they dont already have an swhois field, accept the remote server's
			std::string* loc;
			if (dest->GetExt("location_lng", loc)) {
				dest->Shrink("location_lng");
				delete loc;
			}

			std::string* loc2 = new std::string(extdata);
			dest->Extend("location_lng", loc2);
		}
		if ((target_type == TYPE_CHANNEL) && (extname == "location_lat")) {
			Channel* dest = (Channel*)target;
			// if they dont already have an swhois field, accept the remote server's
			std::string* loc;
			if (dest->GetExt("location_lat", loc)) {
				dest->Shrink("location_lat");
				delete loc;
			}

			std::string* loc2 = new std::string(extdata);
			dest->Extend("location_lat", loc2);
		}
		if ((target_type == TYPE_CHANNEL) && (extname == "location_lng")) {
			Channel* dest = (Channel*)target;
			// if they dont already have an swhois field, accept the remote server's
			std::string* loc;
			if (dest->GetExt("location_lng", loc)) {
				dest->Shrink("location_lng");
				delete loc;
			}

			std::string* loc2 = new std::string(extdata);
			dest->Extend("location_lng", loc2);

			mlocpriv::sendNumericForAllChannel(dest); // I think this is the best place
		}
	}

	virtual ~ModuleSLOC() {
		delete Conf;
		delete cl;
	}

	virtual Version GetVersion() {
		return Version(1, 0, 0, 0, VF_COMMON, API_VERSION);
	}
};

MODULE_INIT(ModuleSLOC)
