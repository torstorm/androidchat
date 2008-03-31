package net.androidchat.client;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//import net.androidchat.client.ServiceIRCService;

public class ActivityChatChannel extends Activity {

    private TextView tv;
    private EditText te;
    private String chan;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
            /*
                case ServiceIRCService.MSG_UPDATECHAN:
                    chan = (String) msg.obj;
                    updateView((String) msg.obj);
                    break;
                    */
            }
        }
    };

    private void updateView(String channel)
    {
        StringBuilder temp = new StringBuilder();
    //    ClassChannelContainer ctemp = (ClassChannelContainer) ServiceIRCService.channels.get(channel);
/*
        if (ctemp == null) return;

        for (int i = 0; i < ctemp.whatsinchan.size(); i++) {
            temp.append(ctemp.whatsinchan.get(i) + "\n");
        }
        */
        tv.setText(">=|");
        this.setTitle("Chat GUI view");
       // this.setTitle(this.getTitle() + " - (" + ctemp.chanusers.size() + ") " + channel + " - " + ctemp.chantopic);
    }

    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.chat);

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.ircsend);
        button.setOnClickListener(mSendListener);
        te = (EditText) findViewById(R.id.ircedit);
        te.setOnKeyListener(mKeyListener);
        tv = (TextView) findViewById(R.id.ircdisp);

      //  ServiceIRCService.ChannelViewHandler = mHandler;

    }

    private OnClickListener mSendListener = new OnClickListener() {
        public void onClick(View v)
        {
            // do the same as the below function
       //     ServiceIRCService.SendToChan(chan, te.getText().toString());
            te.setText("");
        }
    };

    private OnKeyListener mKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int i, KeyEvent k)
        {
            // listen for enter, clear box, send etc
            if (k.getKeyCode() == KeyEvent.KEYCODE_NEWLINE) {
            //    ServiceIRCService.SendToChan(chan, te.getText().toString());
                te.setText("");
                return true;
            }
            return false;
        }

    };
}
