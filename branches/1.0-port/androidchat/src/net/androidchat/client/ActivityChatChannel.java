package net.androidchat.client;

import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class ActivityChatChannel extends Activity {

    private TextView tv;
    private EditText te;
    private ScrollView sv;
    private String CurWindow;
    private ProgressDialog pd;

	private final String PREFS_NAME = "androidChatPrefs";
	SharedPreferences settings; 

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
    	
        StringBuilder temp = new StringBuilder();
        ClassChannelContainer ctemp = (ClassChannelContainer) ServiceIRCService.channels.get(Window);
        if(ServiceIRCService.state >= 10) {
        	
        }
        
        if (ctemp == null)
      	  {
      	  tv.setText("\n\n\n*** You are not in this channel");
      	  return;
      	  }
        if(!Window.equals(CurWindow)) {
      	  
        	if(!Window.equals("~status"))
        	ServiceIRCService.mNM.notify(R.string.irc_started, new Notification(R.drawable.mini_icon, "AndroidChat - Notification", System.currentTimeMillis()));
      	  return;
        }

        for (int i = 0; i < ctemp.whatsinchan.size(); i++) {
            temp.append(ctemp.whatsinchan.get(i) + "\n");
        }
        
        tv.setGravity(0x50);
        tv.setText("\n\n\n" + temp.toString().trim());
        te.setHint(new String(""));
        sv.fullScroll(ScrollView.FOCUS_DOWN);
        sv.smoothScrollBy(0, tv.getLineHeight());
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
        setContentView(R.layout.chat);

		settings = ServiceIRCService.context.getSharedPreferences(PREFS_NAME, 0);

        // Watch for button clicks.
        //Button button = (Button) findViewById(R.id.ircsend);
        //button.setOnClickListener(mSendListener);
        //ImageButton button2 = (ImageButton) findViewById(R.id.ircback);
        //button2.setOnClickListener(mBackListener);
        
        //Button button3 = (Button) findViewById(R.id.ircchannel);
        //button3.setOnClickListener(mMapListener);
        sv = (ScrollView) findViewById(R.id.ircscroll);
        te = (EditText) findViewById(R.id.ircedit);
        te.setOnKeyListener(mKeyListener);
        te.setSingleLine();
        tv = (TextView) findViewById(R.id.ircdisp);
        
        sv.fullScroll(ScrollView.FOCUS_DOWN);
         
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
        menu.add(0, 0, 0, "Channels Map"); // todo: these should pull from a resource
        menu.add(0, 1, 1, "Open Windows");
        //menu.get(0).setIcon(R.drawable.map);
        //menu.get(1).setIcon(R.drawable.channels);       
        menu.add(0, 2, 2, "User Map");
        //menu.get(2).setIcon(R.drawable.dude);
        
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
        	ServiceIRCService.AskForChannelList(); // update channel list
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

        	//Intent u = new Intent(ServiceIRCService.context, ActivityUserMap.class);
        	//startActivity(u);

            return true;
        	} else return false;
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
                return true;
            }
            return false;
        }

    };
    
   
    
   }
