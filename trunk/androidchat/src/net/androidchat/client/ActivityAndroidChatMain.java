package net.androidchat.client;

import android.app.Activity;
import android.content.Intent;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import net.androidchat.client.ActivityChatChannel;

public class ActivityAndroidChatMain
		extends Activity
{
	
	public Intent	myConnectivtyIntent;
	
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		
		// Watch for button clicks.
		Button button = (Button) findViewById(R.id.btn_Connect);
		button.setOnClickListener(mConnectListener);
		if (ServiceIRCService.state == 0)
			button.setText(R.string.btn_connect);
		else
			button.setText(R.string.btn_disconnect);
		
		Button opButton = (Button) findViewById(R.id.btn_ShowOpts);
		opButton.setOnClickListener(mOptionListener);
		//button = (Button) findViewById(R.id.btn_ShowChan);
		//button.setOnClickListener(mChanListener);
	}
	
	private OnClickListener mOptionListener = new OnClickListener() {
		public void onClick(View v) {
			startActivity(new Intent(ActivityAndroidChatMain.this, ActivityOptions.class));

		}
	};
	
	private OnClickListener	mChanListener		= new OnClickListener()
															{
																public void onClick(View v) {
																	// Make sure the service is
																	// started. It will continue
																	// running
																	// until someone calls
																	// stopService().
																	startActivity(new Intent(ActivityAndroidChatMain.this, ActivityChatChannel.class));
																}
															};
															
															
															
															
															
	private OnClickListener	mConnectListener	= new OnClickListener()
															{
																public void onClick(View v) {
																	
																	if (ServiceIRCService.state == 0)
																	{
																		// if not started
																		myConnectivtyIntent = new Intent(ActivityAndroidChatMain.this,
																				ServiceIRCService.class);
																		ComponentName myConnectivtyService = startService(myConnectivtyIntent, null);
																		assert (myConnectivtyService != null);
																		Button button = (Button) findViewById(R.id.btn_Connect);
																		button.setText(R.string.btn_disconnect);
																		startActivity(new Intent(ActivityAndroidChatMain.this, ActivityChatChannel.class));
																	} else
																	{
																		// if started
																		//myConnectivtyIntent = new Intent(ActivityAndroidChatMain.this,		ServiceIRCService.class);
																		ServiceIRCService.QuitServer();
																		stopService(myConnectivtyIntent);
																		Button button = (Button) findViewById(R.id.btn_Connect);
																		button.setText(R.string.btn_connect);
																	}
																}
															};
}
