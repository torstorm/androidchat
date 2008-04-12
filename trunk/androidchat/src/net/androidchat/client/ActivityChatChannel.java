package net.androidchat.client;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ScrollView;
import android.view.Menu;

public class ActivityChatChannel extends Activity {

    private TextView tv;
    private EditText te;
    private ScrollView sv;
    private String CurWindow;



    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
            
                case ServiceIRCService.MSG_UPDATECHAN:
                    updateView((String) msg.obj);
                    break;
                    
                case ServiceIRCService.MSG_CHANGEWINDOW:
               	 	ServiceIRCService.lastwindow = CurWindow;
                	CurWindow = (String) msg.obj; 
                	ServiceIRCService.curwindow = CurWindow;
               	 break;
               	 
  
            }
        }
    };

    public String getCurWindow() {
    	return CurWindow;
    }
    
    private void updateView(String Window)
    {
        StringBuilder temp = new StringBuilder();
        ClassChannelContainer ctemp = (ClassChannelContainer) ServiceIRCService.channels.get(Window);

        if (ctemp == null)
      	  {
      	  tv.setText("\n\n\n*** You are not in this channel");
      	  return;
      	  }
        if(!Window.equals(CurWindow)) {
      	  
        	if(!Window.equals("~status"))
      	  ServiceIRCService.mNM.notify(R.string.irc_started, new Notification(ServiceIRCService.context, R.drawable.mini_icon, ServiceIRCService.context.getText(R.string.ui_newmsg) + " " + Window, System
						.currentTimeMillis(), "AndroidChat - Notification", ServiceIRCService.context.getText(R.string.ui_newmsg) + " " + Window, null, R.drawable.mini_icon,
						"Android Chat", null));
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
       CurWindow = "~status";    

    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        // Parameters for menu.add are:
        // group -- Not used here.
        // id -- Used only when you want to handle and identify the click yourself.
        // title
        menu.add(0, 0, "Channels Map"); // todo: these should pull from a resource
        menu.add(0, 1, "Open Windows");
        menu.add(0,2, "User Map");
        
        menu.get(0).setIcon(R.drawable.map);
        menu.get(1).setIcon(R.drawable.channels);

        return true;
    }

    // Activity callback that lets your handle the selection in the class.
    // Return true to indicate that you've got it, false to indicate
    // that it should be handled by a declared handler object for that
    // item (handler objects are discouraged for reasons of efficiency).
    @Override
    public boolean onOptionsItemSelected(Menu.Item item){
        switch (item.getId()) {
        case 0:
          	ServiceIRCService.AskForChannelList(); // update channel list
        	Intent i = new Intent(ActivityChatChannel.this, AndroidChatMap.class);
            //	i.putExtra("channel_list", ServiceIRCService.channel_list);
    			startActivity(i);
          //  showAlert("Menu Item Clicked", "Zoom", "ok", null, false, null);
            return true;
        case 1:
        	Intent p = new Intent(ActivityChatChannel.this, ChannelGrid.class);
            //	i.putExtra("channel_list", ServiceIRCService.channel_list);
    			startActivity(p);
           // showAlert("Menu Item Clicked", "Settings", "ok", null, false, null);
            return true;
        case 2:
        	Intent u = new Intent(ActivityChatChannel.this, ActivityUserMap.class);
        	u.putExtra("name", CurWindow);
        	startActivity(u);
           // showAlert("Menu Item Clicked", "Other", "ok", null, false, null);
            return true;
        }
        return false;
    }
    private OnClickListener mSendListener = new OnClickListener() {
        public void onClick(View v)
        {
            // do the same as the below function
           ServiceIRCService.SendToChan(CurWindow, te.getText().toString());
            te.setText("");
        }
    };

    private OnKeyListener mKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int i, KeyEvent k)
        {
            // listen for enter, clear box, send etc
            if (k.getKeyCode() == KeyEvent.KEYCODE_NEWLINE) {
                ServiceIRCService.SendToChan(CurWindow, te.getText().toString());
                te.setText("");
                return true;
            }
            return false;
        }

    };
    
    private OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v)
        {
            // do the same as the below function
       //     ServiceIRCService.SendToChan(chan, te.getText().toString());
            finish();
        }
    };
    
    private OnClickListener mMapListener = new OnClickListener() {
        public void onClick(View v)
        {
        	Intent i = new Intent(ActivityChatChannel.this, AndroidChatMap.class);
        //	i.putExtra("channel_list", ServiceIRCService.channel_list);
			startActivity(i);

            // do the same as the below function
       //     ServiceIRCService.SendToChan(chan, te.getText().toString());
        }
    };
    
   }
