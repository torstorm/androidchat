package net.androidchat.client;

import android.app.Activity;
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

public class ActivityChatChannel extends Activity {

    private TextView tv;
    private EditText te;
    private String chan;
    private ScrollView sv;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
            
                case ServiceIRCService.MSG_UPDATECHAN:
                    chan = (String) msg.obj;
                    updateView((String) msg.obj);
                    break;
                    
            }
        }
    };

    private void updateView(String channel)
    {
        StringBuilder temp = new StringBuilder();
        ClassChannelContainer ctemp = (ClassChannelContainer) ServiceIRCService.channels.get(channel);

        if (ctemp == null)
      	  {
      	  tv.setText("\n\n\n*** You are not in this channel");
      	  return;
      	  }

        for (int i = 0; i < ctemp.whatsinchan.size(); i++) {
            temp.append(ctemp.whatsinchan.get(i) + "\n");
        }
        
        tv.setGravity(0x50);
        tv.setText("\n\n\n" + temp.toString().trim());
       // te.setHint(new String("lines: " + tv.getLineCount()));
        sv.fullScroll(ScrollView.FOCUS_DOWN);
        sv.smoothScrollBy(0, tv.getLineHeight());
        this.setTitle(R.string.app_name);
        this.setTitle(this.getTitle() + " - (" + ctemp.chanusers.size() + ") " + channel + " - " + ctemp.chantopic);
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


       ServiceIRCService.ChannelViewHandler = mHandler;
    }

    private OnClickListener mSendListener = new OnClickListener() {
        public void onClick(View v)
        {
            // do the same as the below function
           ServiceIRCService.SendToChan(chan, te.getText().toString());
            te.setText("");
        }
    };

    private OnKeyListener mKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int i, KeyEvent k)
        {
            // listen for enter, clear box, send etc
            if (k.getKeyCode() == KeyEvent.KEYCODE_NEWLINE) {
                ServiceIRCService.SendToChan(chan, te.getText().toString());
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