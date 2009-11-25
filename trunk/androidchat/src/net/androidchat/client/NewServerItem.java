package net.androidchat.client;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewServerItem extends Activity {

	@Override
	public void onCreate(Bundle appState) {
		super.onCreate(appState);
		
		setContentView(R.layout.new_server);
		
		Button btn = (Button)findViewById(R.id.new_confirm);
		btn.setOnClickListener(mAddListener);
		
	}
	
	private OnClickListener mAddListener = new OnClickListener() {
		public void onClick(View v) {
			//add the server to the database.
			addServer();
		}
	};
	
	private void addServer() {
		EditText title = (EditText)findViewById(R.id.new_title);
		EditText address = (EditText)findViewById(R.id.new_address);
		
		if (title.getText().toString().equals("") || address.getText().toString().equals("")) {
			Toast.makeText(this, "Please fill out both fields", Toast.LENGTH_LONG).show();
			return;
		}
		
		ACDatabaseAdapter db = new ACDatabaseAdapter(this);
		
		long id = db.addServer(title.getText().toString(), address.getText().toString());
		
		db.close();
		
		if (id != -1)
			finish();
		else
			Toast.makeText(this, "Error saving new server.", Toast.LENGTH_LONG).show();
	}
}
