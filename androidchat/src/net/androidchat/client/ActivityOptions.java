package net.androidchat.client;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Button;


import net.androidchat.client.ActivityChatChannel;
import net.androidchat.client.ServiceIRCService;

public class ActivityOptions extends Activity {
	public static final String PREFS_NAME = "androidChatPrefs";
	private EditText nickText;
	private CheckBox locBox;
	private CheckBox pmBox;


	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.options);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

	    Button saveButton = (Button) findViewById(R.id.optSave);   
	    saveButton.setOnClickListener(mSaveListener);
	       
        nickText = (EditText) findViewById(R.id.optNick);
	    locBox = (CheckBox) findViewById(R.id.optSend);
	    pmBox = (CheckBox) findViewById(R.id.optPMAlert);

        this.setTitle("AndroidChat Options");
        locBox.setChecked(settings.getBoolean("sendLoc", true));
        pmBox.setChecked(settings.getBoolean("pmAlert", true));
        nickText.setText(settings.getString("defNick", "AndroidChat"));
        //settings.getBoolean("sendLoc", true);
        


	}
	
	private OnClickListener mSaveListener = new OnClickListener() {
		public void onClick(View v) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			
			editor.putBoolean("sendLoc", locBox.isChecked());
			editor.putBoolean("pmAlert", pmBox.isChecked());

			editor.putString("defNick", nickText.getText().toString());
			editor.commit();
			finish();
		//	startActivity(new Intent(ActivityAndroidChatMain.this, ActivityOptions.class));

		}
	};
}
