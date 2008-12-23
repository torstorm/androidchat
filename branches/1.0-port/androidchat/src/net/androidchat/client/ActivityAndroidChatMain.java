package net.androidchat.client;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActivityAndroidChatMain extends Activity {

	public Intent myConnectivtyIntent;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		if (wm.getDefaultDisplay().getHeight() < 300) // quick hack so we're
														// never off the screen
			setContentView(R.layout.main_alt); // fix 
		else
			setContentView(R.layout.main);

		// Watch for button clicks.
		Button button = (Button) findViewById(R.id.btn_Connect);
		button.setOnClickListener(mConnectListener);
		if (ServiceIRCService.state <= 0)
			button.setText(R.string.btn_connect);
		else
			button.setText(R.string.btn_disconnect);

		Button opButton = (Button) findViewById(R.id.btn_ShowOpts);
		if (ServiceIRCService.state <= 0)
			opButton.setText(R.string.btn_options);
		else
			opButton.setText(R.string.btn_showwind);

		opButton.setOnClickListener(mOptionListener);

	}

	private OnClickListener mOptionListener = new OnClickListener() {
		public void onClick(View v) {
			if (ServiceIRCService.state <= 0)

				startActivity(new Intent(ActivityAndroidChatMain.this,
						ActivityOptions.class));
			else
					{
				
					startActivity(new Intent(ActivityAndroidChatMain.this,
							ActivityChatChannel.class));
					}
		}
	};

	private OnClickListener mConnectListener = new OnClickListener() {
		public void onClick(View v) {
			Button opButton = (Button) findViewById(R.id.btn_ShowOpts);
			if (ServiceIRCService.state <= 0) {
				// if not started
				myConnectivtyIntent = new Intent(ActivityAndroidChatMain.this,
						ServiceIRCService.class);
				ComponentName myConnectivtyService = startService(
						myConnectivtyIntent, null);
				assert (myConnectivtyService != null);
				Button button = (Button) findViewById(R.id.btn_Connect);
				button.setText(R.string.btn_disconnect);
				opButton.setText(R.string.btn_showwind);
			
				startActivity(new Intent(ActivityAndroidChatMain.this,
						ActivityChatChannel.class));
			} else {
				// if started
				// myConnectivtyIntent = new
				// Intent(ActivityAndroidChatMain.this,
				// ServiceIRCService.class);
				ServiceIRCService.QuitServer();
				//stopService(myConnectivtyIntent);
				Button button = (Button) findViewById(R.id.btn_Connect);
				button.setText(R.string.btn_connect);
				opButton.setText(R.string.btn_options);
			}
		}
	};
}
