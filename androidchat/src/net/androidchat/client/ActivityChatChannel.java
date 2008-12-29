package net.androidchat.client;

import android.app.ListActivity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnKeyListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Vector;

public class ActivityChatChannel extends ListActivity {

	private static final int OPEN_WINDOWS = Menu.FIRST;
	private static final int CLOSE_WINDOW = Menu.FIRST + 1;
	private static final int DISCONNECT = Menu.FIRST + 2;
	private static final int SHOW_USER_LIST = Menu.FIRST + 3;
	
    private TextView tv;
    private EditText te;
    private ScrollView sv;
    private String CurWindow;
    private ProgressDialog pd;

	private final String PREFS_NAME = "androidChatPrefs";
	SharedPreferences settings; 
	
	private ChatAdapter mAdapter;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
               
            	
            	case ServiceIRCService.MSG_CHANGEWINDOW:
               		ServiceIRCService.lastwindow = CurWindow;
               		CurWindow = (String) msg.obj; 
               		ServiceIRCService.curwindow = CurWindow;
            	 
                case ServiceIRCService.MSG_UPDATECHAN:
                    updateView((String) msg.obj);
                    break;
                    
                case ServiceIRCService.MSG_CLEARPROGRESS:
               	 if (pd != null)
               		 pd.dismiss();
               	 break;
               	 
  
            }
        }
    };

    public String getCurWindow() {
    	return CurWindow;
    }
    
    private void updateView(String Window)
    {

    	if(ServiceIRCService.state == 10) {
    		if(!ServiceIRCService.shownChanListConnect) {
    	    	
    			if(settings.getBoolean("showList", false))
    			{
    			
                	//Intent i = new Intent(ServiceIRCService.context, AndroidChatMap.class);
        			//startActivity(i);    		
        		} else {
    			
    			if(pd != null)
    				pd.dismiss();
        		}
    			ServiceIRCService.shownChanListConnect = true;

    		}
    	}
    	
        //StringBuilder temp = new StringBuilder();
        ClassChannelContainer ctemp = (ClassChannelContainer) ServiceIRCService.channels.get(Window);
        if(ServiceIRCService.state >= 10) {
        	
        }
        
        if (ctemp == null)
      	  {
      	  //tv.setText("\n\n\n*** You are not in this channel");
      	  return;
      	  }
        if(!Window.equals(CurWindow)) {
      	  
        	if(!Window.equals("~status"))
        	//ServiceIRCService.mNM.notify(R.string.irc_started, new Notification(R.drawable.mini_icon, "AndroidChat - Notification", System.currentTimeMillis()));
      	  return;
        }
        mAdapter.clear();
        for (int i = 0; i < ctemp.whatsinchan.size(); i++) {
        	mAdapter.loadData(ctemp.whatsinchan.get(i));
            //temp.append(ctemp.whatsinchan.get(i) + "\n");
        }
        mAdapter.refresh();
        //mAdapter.loadData(ctemp.whatsinchan.get(ctemp.whatsinchan.size() - 1));
        
        //getListView().scrollTo(0, getListView().getBottom());
        //tv.setGravity(0x50);
        //tv.setText("\n\n\n" + temp.toString().trim());
        //te.setHint(new String(""));
        //sv.fullScroll(ScrollView.FOCUS_DOWN);
        //sv.smoothScrollBy(0, tv.getBottom());
        
        this.setTitle(R.string.app_name);
        if (ctemp.IS_PM)
        {
           this.setTitle(this.getTitle() + " - Private Message with " + ctemp.channame);

        } else if (ctemp.IS_STATUS)
        {
           this.setTitle(this.getTitle() + " - Status Window");

        } else {
           this.setTitle(this.getTitle() + " - (" + ctemp.chanusers.size() + ") " + Window + " - " + ctemp.chantopic);
        }
    }
    

    @Override
    protected void onDestroy()
    {
    super.onDestroy();
    ServiceIRCService.ChannelViewHandler = null;
    }
    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        
        mAdapter = new ChatAdapter(this);
        setListAdapter(mAdapter);
        
        setContentView(R.layout.chat);

		settings = ServiceIRCService.context.getSharedPreferences(PREFS_NAME, 0);

		getListView().setStackFromBottom(true);
		getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		
        // Watch for button clicks.
        //Button button = (Button) findViewById(R.id.ircsend);
        //button.setOnClickListener(mSendListener);
        //ImageButton button2 = (ImageButton) findViewById(R.id.ircback);
        //button2.setOnClickListener(mBackListener);
        
        //Button button3 = (Button) findViewById(R.id.ircchannel);
        //button3.setOnClickListener(mMapListener);
        //sv = (ScrollView) findViewById(R.id.ircscroll);
        te = (EditText) findViewById(R.id.ircedit);
        te.setOnKeyListener(mKeyListener);
        te.setSingleLine();
        //tv = (TextView) findViewById(R.id.ircdisp);
        
       //sv.fullScroll(ScrollView.FOCUS_DOWN);
         
       ServiceIRCService.SetViewHandler(mHandler);
       if(!ServiceIRCService.shownChanListConnect)
       {
       pd = ProgressDialog.show(this, "Working..", "Establishing Network Connection", true,
             false);
       }
       CurWindow = "~status";    		
       
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        // Parameters for menu.add are:
        // group -- Not used here.
        // id -- Used only when you want to handle and identify the click yourself.
        // title
        //menu.add(0, 0, 0, "Channels Map"); // todo: these should pull from a resource
        menu.add(0, OPEN_WINDOWS, 1, "Open Windows").setIcon(R.drawable.ic_menu_chat_dashboard);
        menu.add(0, CLOSE_WINDOW, 2, "Close Window").setIcon(R.drawable.ic_menu_end_conversation);
        menu.add(0, DISCONNECT, 3, "Disconnect").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        menu.add(0, SHOW_USER_LIST, 4, "Show Users").setIcon(R.drawable.ic_menu_friend_list);
        
        return true;
    }

    // Activity callback that lets your handle the selection in the class.
    // Return true to indicate that you've got it, false to indicate
    // that it should be handled by a declared handler object for that
    // item (handler objects are discouraged for reasons of efficiency).
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
        case 0:
      	  pd = ProgressDialog.show(this, "Working..", "Updating Channel List", true,
                 false);
        	//ServiceIRCService.AskForChannelList(); // update channel list
        	//Intent i = new Intent(ServiceIRCService.context, AndroidChatMap.class);
    			//startActivity(i);
            return true;
        case 1:
        	Intent p = new Intent(ServiceIRCService.context, ChannelGrid.class);
    			startActivity(p);
            return true;
        case 2:
        	if((!ServiceIRCService.channels.get(ServiceIRCService.curwindow.toLowerCase()).IS_PM) && (!ServiceIRCService.channels.get(ServiceIRCService.curwindow.toLowerCase()).IS_STATUS))
        	{
        	 pd = ProgressDialog.show(this, "Working..", "Updating Users List", true,
                false);
        	 return true;
        	} else return false;
        case 4:
        	ServiceIRCService.QuitServer();
        	finish();
        	break;
        }
        return false;
    }
   

    private OnKeyListener mKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int i, KeyEvent k)
        {
            // listen for enter, clear box, send etc
            if (k.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                ServiceIRCService.SendToChan(CurWindow, te.getText().toString());
                te.setText("");
                //sv.scrollTo(0, tv.getBottom());
                return true;
            }
            return false;
        }

    };
    
   
    //chat list adapter.
    public class ChatAdapter extends BaseAdapter {
    	
    	private Context mContext;
    	private Vector<String> mSenders;
    	private Vector<String> mMessages;
    	
    	public ChatAdapter(Context c) {
    		mContext = c;
    		
    		mSenders = new Vector<String>();
    		mMessages = new Vector<String>();
    	}
    	
    	public int getCount() {
    		return mSenders.size();
    	}
    	
    	public Object getItem(int position) {
    		return mMessages.elementAt(position);
    	}
    	
    	public long getItemId(int position) {
    		return position;
    	}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
    		
    		LayoutInflater inflate = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		View row = inflate.inflate(R.layout.chat_row, parent, false);
    		
    		//place the item in the row.
    		TextView sender = (TextView)row.findViewById(R.id.message_sender);
    		TextView message = (TextView)row.findViewById(R.id.message_body);
    		
    		sender.setTextColor(Color.BLUE);
    		
    		if (mSenders.elementAt(position).startsWith("*")) {
    			sender.setTypeface(sender.getTypeface(), Typeface.BOLD_ITALIC);
    			message.setTypeface(sender.getTypeface(), Typeface.BOLD_ITALIC);
    		}
    		
    		if (mSenders.elementAt(position).equals(ServiceIRCService.nick)) 
    			sender.setTextColor(Color.GRAY);
    		
    		sender.setText(mSenders.elementAt(position));
    		
    		message.setTextColor(Color.BLACK);
    		message.setText(mMessages.elementAt(position));
    		
    		return row;
    	}
    	
    	public void loadData(String raw) {
    		if (raw.contains("~+")) {
    			mSenders.add(raw.substring(0, raw.indexOf("~+")));
    			
    			if (raw.contains("ACTION"))
    				mMessages.add(raw.substring(raw.indexOf("ACTION") + 6));
    			else
    				mMessages.add(raw.substring(raw.indexOf("~+") + 2));
    		} else {
    			mSenders.add(" ");
    			mMessages.add(raw);
    		}
    		
    		notifyDataSetChanged();
    	}
    	
    	public void clear() {
    		mSenders = new Vector<String>();
    		mMessages = new Vector<String>();
    	}
    	
    	public void refresh() {
    		//notifyDataSetInvalidated();
    	}

    }
    
   }
