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
	private static String											nick				= "AndroidChat";
	
	public static final int											MSG_UPDATECHAN	= 0;
	public static final int											MSG_UPDATEPM	= 1;
	public static final int											MSG_CHANGECHAN	= 2;
	public static final int											MSG_DISCONNECT	= 3;
	
	public static final String										AC_VERSION = "0.01A";
	
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
				temp.addLine("*** Channel Location Updated");
				// should probably restrict this to debug
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
				temp.addLine("Topic for " + toks[3] + " is: " + args);
				temp.chantopic = args;
				if (ChannelViewHandler != null)
					Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, chan).sendToTarget();
			} // ignore topics for channels we aren't in
		} else if (command.equals("JOIN"))
		// User must have joined a channel
		{
			ClassChannelContainer temp;
			if (channels.containsKey(args.toLowerCase())) // existing channel?
			{
				temp = channels.get(args);
			} else
			{
				temp = new ClassChannelContainer();
				temp.channame = args;
				temp.addLine("Now talking on " + args + "...");
				channels.put(args.toLowerCase(), temp);
			}
			if (ChannelViewHandler != null)
				Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, args.toLowerCase()).sendToTarget();
			
		} else if (command.equals("PRIVMSG"))
		// to a channel?
		{
			ClassChannelContainer temp;
			String chan = toks[2].toLowerCase();
			
			if (channels.containsKey(chan)) // existing channel?
			{
				temp = channels.get(chan);
				temp.addLine("<" + toks[0].substring(1, toks[0].indexOf("!")) + "> " + args);
				if (ChannelViewHandler != null)
					Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, toks[2].toLowerCase()).sendToTarget();
			}
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
			try
			{
				String temp = what.toUpperCase().substring(1) + "\n";
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
