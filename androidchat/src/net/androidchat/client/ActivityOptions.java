package net.androidchat.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class ActivityOptions extends Activity {
	public static final String PREFS_NAME = "androidChatPrefs";
	private EditText nickText;
	private EditText ajText;
	
	private CheckBox listBox;
	private CheckBox locBox;
	private CheckBox pmBox;


	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.options);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

	    Button saveButton = (Button) findViewById(R.id.optSave);   
	    saveButton.setOnClickListener(mSaveListener);
	    
	    Button cancButton = (Button) findViewById(R.id.optCancel);   
	    cancButton.setOnClickListener(mCancListener);
	    
        nickText = (EditText) findViewById(R.id.optNick);
        ajText = (EditText) findViewById(R.id.optAutoJoin);
	    
        locBox = (CheckBox) findViewById(R.id.optSend);
	    pmBox = (CheckBox) findViewById(R.id.optPMAlert);
	    listBox = (CheckBox) findViewById(R.id.optList);

        this.setTitle("AndroidChat Options");
        
        listBox.setChecked(settings.getBoolean("showList", true));
        locBox.setChecked(settings.getBoolean("sendLoc", true));
        pmBox.setChecked(settings.getBoolean("pmAlert", true));
        nickText.setText(settings.getString("defNick", "AndroidChat"));
        ajText.setText(settings.getString("autoJoin", ""));
        //settings.getBoolean("sendLoc", true);

	} 
	
	private OnClickListener mCancListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};
	
	private OnClickListener mSaveListener = new OnClickListener() {
		public void onClick(View v) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit(); 
			
			if(nickText.getText().toString().matches("[A-Za-z][0-9a-zA-Z\\[\\]\\\\^\\-_`{|}]{0,29}"))
			{
			editor.putBoolean("sendLoc", locBox.isChecked());
			editor.putBoolean("pmAlert", pmBox.isChecked());
			editor.putBoolean("showList", listBox.isChecked());
			
			editor.putString("defNick", nickText.getText().toString());
			editor.putString("autoJoin", ajText.getText().toString());
			
			editor.commit();
			finish();
			} else 
			{
				//static 	AlertDialog 	show(Context context, CharSequence title, int iconId, CharSequence message, CharSequence buttonText, boolean cancelable)
				//TODO: fix the alert dialog here, this is low priority right now. 
				//AlertDialog.show(ActivityOptions.this, "Invalid Nickname", R.drawable.stop, "Your nickname contains invalid characters or is too long.", "Okay", true);
			}
		//	startActivity(new Intent(ActivityAndroidChatMain.this, ActivityOptions.class));

		}
	};
}
