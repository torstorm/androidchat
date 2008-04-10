package net.androidchat.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.Context;

import android.os.*;

import android.os.Binder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.ArrayList;

public class ServiceIRCService
		extends Service
{
	private Context													context;
	private static Thread											connection;
	private static Thread											updates;
	
	private static Socket											socket;
	public static BufferedWriter									writer;
	public static BufferedReader									reader;
	public static int													state;
	private static String											server			= "38.100.42.254";
	private static String											nick				= "AndroidChat2";
	
	public static final int											MSG_UPDATECHAN	= 0;
	public static final int											MSG_UPDATEPM	= 1;
	public static final int											MSG_CHANGECHAN	= 2;
	public static final int											MSG_DISCONNECT	= 3;
	
	public static final String										AC_VERSION		= "0.01A";
	
	private static boolean											is_first_list;
	
	public static HashMap<String, ClassChannelContainer>	channels;
	public static HashMap<String, ClassChannelDescriptor>	channel_list;
	
	public static Handler											ChannelViewHandler;
	
	// this is epic irc parsing.
	public static void GetLine(String line) {
		// rfc 2812
		// [:prefix] command|numeric [arg1, arg2...] :extargs
		
		String args, prefix, command;
		args = prefix = command = "";
		
		System.out.println("debug: " + line);
		
		boolean flagupdate = false;
		String updatechan = "";
		
		// pull off extended arguments first
		if (line.indexOf(":", 2) != -1)
			args = line.substring(line.indexOf(":", 2) + 1).trim();
		
		// if we have extended arguments, remove them from the parsing
		if (args.length() > 0)
			line = line.substring(0, line.length() - args.length());
		
		String[] toks = line.split(" "); // split by spaces.
		
		if (toks[0].startsWith(":")) // we have a prefix
		{
			prefix = toks[0].substring(1);
			command = toks[1];
		} else
		{
			prefix = null;
			command = toks[0];
		}
		
		if (command.equals("641"))
		// :servername 641 yournick #channel lat long
		{
			ClassChannelContainer temp;
			String chan = toks[3].toLowerCase();
			if (channels.containsKey(chan))
			{
				temp = channels.get(chan);
				temp.loc_lat = Float.parseFloat(toks[4]);
				temp.loc_lng = Float.parseFloat(toks[5]);
			}
			
			if (channel_list.containsKey(chan))
			{
				ClassChannelDescriptor t = channel_list.get(chan);
				t.loc_lat = Float.parseFloat(toks[4]);
				t.loc_lng = Float.parseFloat(toks[5]);
			}
		} else if (command.equals("323")) // list end numeric
		{
			is_first_list = true;
		} else if (command.equals("322")) // list numeric
		{ // 0 1 2 3 4 args
			// :servername 322 yournick <channel> <#_visible> :<topic>
			
			if (is_first_list)
			{
				channel_list.clear();
				is_first_list = false;
			}
			/*
			 * if (channel_list.containsKey(toks[2])) { ClassChannelDescriptor t =
			 * channel_list.get(toks[2]); t.channame = toks[2]; t.chantopic = args;
			 * t.chatters = Integer.parseInt(toks[3]);
			 * 
			 * RequestChannelLocation(toks[2]); } else {
			 */
			ClassChannelDescriptor t = new ClassChannelDescriptor();
			t.channame = toks[3];
			t.chantopic = args;
			t.chatters = Integer.parseInt(toks[4]);
			channel_list.put(toks[3], t);
			
			RequestChanLocation(toks[3]);
			// }
			
		} else if (command.equals("331") || command.equals("332")) // topic
		// numeric
		// :servername 331 yournick #channel :no topic
		// :servername 332 yournick #channel :topic here
		{
			ClassChannelContainer temp;
			String chan = toks[3].toLowerCase();
			if (channels.containsKey(chan))
			{
				temp = channels.get(chan);
				temp.addLine("*** Topic for " + toks[3] + " is: " + args);
				temp.chantopic = args;
				flagupdate = true;
				updatechan = chan;
			} // ignore topics for channels we aren't in
			
		} else if (command.equals("353"))
		{
			// :dexter.chatspike.net 353 yournick = #funfactory :chattie dizz
			// @+takshaka
			// poshgal @LDI Aldebaran Sweet2 James54
			if (channels.containsKey(toks[4].toLowerCase()))
			{
				String[] incoming = args.split(" ");
				ClassChannelContainer c = channels.get(toks[4].toLowerCase());
				if (c.NAMES_END == true)
				{
					c.NAMES_END = false;
					c.chanusers.clear();
				}
				
				for (String s : incoming)
				{
					c.chanusers.add(s.replace('@', ' ').replace('+', ' ').trim());
				}
				
			}
		} else if (command.equals("366"))
		{
			// :dexter.chatspike.net 366 t #testtest :End of /NAMES list.
			String chan = toks[3].toLowerCase();
			if (channels.containsKey(chan))
			{
				channels.get(chan).NAMES_END = true;
				
				StringBuilder sb = new StringBuilder();
				
				sb.append("*** Users on ").append(toks[3]).append(": ");
				for (String s : channels.get(chan).chanusers)
				{
					sb.append(s).append(" ");
				}
				
				channels.get(chan).addLine(sb.toString().trim());
				
				flagupdate = true;
				updatechan = chan;
			}
		} else if (command.equals("NICK"))
		{
			// :nick!mask@mask NICK newnick
			// :Testing!AndroidChat@71.61.229.105 NICK Poop
			
			String oldnick = toks[0].substring(1, toks[0].indexOf("!"));
			if (nick.toLowerCase().equals(oldnick.toLowerCase())) // we changed
																					// /our/ nickname
			{
				nick = toks[2];
			}
			
			for (ClassChannelContainer c : channels.values())
			{
				for (String s : c.chanusers)
				{
					if (s.toLowerCase().equals(oldnick.toLowerCase()))
					{
						c.chanusers.remove(s);
						c.chanusers.add(toks[2]);
						c.addLine("*** " + oldnick + " is now known as " + toks[2]);
						if (ChannelViewHandler != null)
							Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, c.channame.toLowerCase()).sendToTarget();
						break;
					}
				}
			}
			// todo: notify of nick renames here
		} else if (command.equals("QUIT"))
		{
			// :Jeff!Kuja@71.61.229.105 QUIT :
			String whoquit = toks[0].substring(1, toks[0].indexOf("!"));
			
			for (ClassChannelContainer c : channels.values())
			{
				for (String s : c.chanusers)
				{
					if (s.toLowerCase().equals(whoquit.toLowerCase()))
					{
						c.chanusers.remove(s);
						c.addLine("*** " + whoquit + " has disconnected (" + args + ")");
						if (ChannelViewHandler != null)
							Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, c.channame.toLowerCase()).sendToTarget();
						break;
					}
				}
			}
			
		} else if (command.equals("KICK"))
		{
			// :prefix kick #chan who :why
			if (channels.containsKey(toks[2].toLowerCase()))
			{
				ClassChannelContainer c = channels.get(toks[2].toLowerCase());
				if (c.chanusers.contains(toks[3]))
					c.chanusers.remove(toks[3]);
				c.addLine("*** " + toks[3] + " was kicked (" + args + ")");
				flagupdate = true;
				updatechan = toks[2];
			}
		} else if (command.equals("PART"))
		// User must have left a channel
		{
			// :Kraln!Kraln@71.61.229.105 PART #hi
			String who = toks[0].substring(1, toks[0].indexOf("!"));
			ClassChannelContainer temp;
			if (who.equals(nick)) // if we joined a channel
			{
				
				if (channels.containsKey(toks[2].toLowerCase())) // existing channel?
				{
					temp = channels.get(toks[2].toLowerCase());
					temp.addLine("*** You have left this channel.");
					if (ChannelViewHandler != null)
						Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, temp.channame.toLowerCase()).sendToTarget();
					channels.remove(temp); // will this work?
				}
			} else
			{
				temp = channels.get(toks[2].toLowerCase());
				temp.chanusers.remove(who);
				temp.addLine("*** " + who + " has left the channel.");
			}
			flagupdate = true;
			updatechan = toks[2].toLowerCase();
		} else if (command.equals("JOIN"))
		// User must have joined a channel
		{
			String who = toks[0].substring(1, toks[0].indexOf("!"));
			ClassChannelContainer temp;
			if (who.equals(nick)) // if we joined a channel
			{
				
				if (channels.containsKey(args.toLowerCase())) // existing channel?
				{
					temp = channels.get(args.toLowerCase());
				} else
				{
					temp = new ClassChannelContainer();
					temp.channame = args.toLowerCase();
					temp.addLine("*** Now talking on " + args + "...");
					channels.put(args.toLowerCase(), temp);
				}
			} else
			{
				temp = channels.get(args.toLowerCase());
				temp.chanusers.add(who);
				temp.addLine("*** " + who + " has joined the channel.");
			}
			flagupdate = true;
			updatechan = args.toLowerCase();
			
		} else if (command.equals("PRIVMSG"))
		// to a channel?
		{
			ClassChannelContainer temp;
			String chan = toks[2].toLowerCase();
			
			if (channels.containsKey(chan)) // existing channel?
			{
				temp = channels.get(chan);
				if (args.trim().startsWith("ACTION"))
				{
					temp.addLine("* " + toks[0].substring(1, toks[0].indexOf("!")) + " " + args.substring(7));
					
				} else
					temp.addLine("<" + toks[0].substring(1, toks[0].indexOf("!")) + "> " + args);
				
				flagupdate = true;
				updatechan = chan;
			}
		}
		
		if (flagupdate)
		{
			if (ChannelViewHandler != null)
				Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, updatechan.toLowerCase()).sendToTarget();
		}
		
	}
	
	public static void AskForChannelList() {
		
		try
		{
			String temp = "LIST\n";
			writer.write(temp);
			writer.flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
	}
	
	// ask server to send channel location
	public static void RequestChanLocation(String chan) {
		try
		{
			String temp = "gcloc " + chan + "\n";
			writer.write(temp);
			writer.flush();
			
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
	}
	
	// send a location to the server.
	public static void UpdateLocation(double lat, double lng) {
		// SLOC lat lng
		try
		{
			String temp = "sloc " + lat + " " + lng + "\n";
			writer.write(temp);
			writer.flush();
			
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
	}
	
	public static void QuitServer() {
		try
		{
			String temp = "QUIT :Android Client has quit";
			writer.write(temp);
			writer.flush();
			
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
	}
	
	public static void SendToChan(String chan, String what) {
		if (what.trim().equals(""))
			return;
		if (chan == null)
		{
			// error about not being on a channel here
			return;
		}
		
		if (what.startsWith("/"))
		{
			// this is a raw command.
			// parse here for intelligent commands, otherwise send it along raw
			
			if (what.startsWith("/me ")) // special case...
			{
				try
				{
					String temp = "PRIVMSG " + chan + " :" + '\001' + "ACTION " + what.substring(4) + '\001' + "\n";
					
					writer.write(temp);
					writer.flush();
					GetLine(":" + nick + "! " + temp);
					
				} catch (IOException e)
				{
					e.printStackTrace();
				} catch (NullPointerException npe)
				{
					npe.printStackTrace();
				}
				if (ChannelViewHandler != null)
					Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, chan).sendToTarget();
				return;
			}
			
			try
			{
				String temp = what.substring(1) + "\n";
				writer.write(temp);
				writer.flush();
			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (NullPointerException npe)
			{
				npe.printStackTrace();
			}
			return;
			
		}
		
		// PRIVMSG <target> :<message>
		try
		{
			String temp = "PRIVMSG " + chan + " :" + what + "\n";
			GetLine(":" + nick + "! " + temp);
			writer.write(temp);
			writer.flush();
			if (ChannelViewHandler != null)
				Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, chan).sendToTarget();
			
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
	}
	
	@Override
	protected void onCreate() {
		mNM = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
		// This is who should be launched if the user selects our persistent
		// notification.
		context = this;
		Intent intent = new Intent();
		intent.setClass(this, ActivityAndroidChatMain.class);
		
		channels = new HashMap<String, ClassChannelContainer>();
		channel_list = new HashMap<String, ClassChannelDescriptor>();
		
		is_first_list = true;
		
		ClassChannelContainer debug = new ClassChannelContainer();
		debug.channame = "Status/Debug Window";
		debug.addLine("AndroidChat v" + AC_VERSION + " started.");
		channels.put("status", debug);
		
		if (ChannelViewHandler != null)
			Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, "status").sendToTarget();
		
		// Display a notification about us starting. We use both a transient
		// notification and a persistent notification in the status bar.
		mNM.notify(R.string.irc_started, new Notification(context, R.drawable.mini_icon, getText(R.string.irc_started), System
				.currentTimeMillis(), "AndroidChat - Notification", getText(R.string.irc_started), intent, R.drawable.mini_icon,
				"Android Chat", intent));
		
		connection = new Thread(new ThreadConnThread(server, nick, socket));
		connection.start();
		
		updates = new Thread(new ThreadUpdateLocThread(context));
		updates.start();
		
		mNM.notify(R.string.irc_started, new Notification(context, R.drawable.mini_icon, getText(R.string.irc_connected), System
				.currentTimeMillis(), "AndroidChat - Notification", getText(R.string.irc_connected), null, R.drawable.mini_icon,
				"Android Chat", null));
		
	}
	
	@Override
	protected void onDestroy() {
		// Cancel the persistent notification.
		QuitServer();
		
		mNM.cancel(R.string.irc_started);
		connection.interrupt();
		state = 0;
		
		// Tell the user we stopped.
		mNM.notify(R.string.irc_started, new Notification(context, R.drawable.mini_icon, getText(R.string.irc_stopped), System
				.currentTimeMillis(), "AndroidChat - Notification", getText(R.string.irc_stopped), null, R.drawable.mini_icon, "Android Chat",
				null));
	}
	
	public IBinder onBind(Intent intent) {
		return getBinder();
		
	}
	
	public IBinder getBinder() {
		return mBinder;
	}
	
	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder			mBinder	= new Binder()
														{
															@Override
															protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
																return super.onTransact(code, data, reply, flags);
															}
														};
	
	private NotificationManager	mNM;
}
