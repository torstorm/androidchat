package net.androidchat.client;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Vector;

public class ActivityAndroidChatMain extends ListActivity {

	public Intent myConnectivtyIntent;

	private static final int NEW_SERVER = Menu.FIRST;
	private static final int SETTINGS_ID = Menu.FIRST + 2;
	private static final int ACTIVE_CHATS = Menu.FIRST + 1;
	
	private ServerListAdapter mAdapter;
	private Vector<String> connections = new Vector<String>();
	
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		mAdapter = new ServerListAdapter(this);
		mAdapter.loadData();
		
		setListAdapter(mAdapter);
		
		setContentView(R.layout.main);

	}

	@Override
	public void onStart() {
		super.onStart();
		
		mAdapter.loadData();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		//if (ServiceIRCService.state > 0) menu.add(0, ACTIVE_CHATS, 0, "Active Sessions");
		menu.add(0, NEW_SERVER, 0, "Add New Server").setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, SETTINGS_ID, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case SETTINGS_ID:
				startActivity(new Intent(this, IRCPreferences.class));
				break;
			case ACTIVE_CHATS:
				Intent p = new Intent(ServiceIRCService.context, ChannelGrid.class);
    			startActivity(p);
    			break;
			case NEW_SERVER:
				startActivity(new Intent(this, NewServerItem.class));
			default:
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
			Intent newConn = new Intent(ActivityAndroidChatMain.this,
					ServiceIRCService.class);
			
			newConn.putExtra("server", (String)mAdapter.getItem(position));
			
			SharedPreferences settings = getSharedPreferences("androidChatPrefs", Context.MODE_WORLD_READABLE);
			String nick = settings.getString("irc_nickname_key", "AndroidChat");
			
			newConn.putExtra("nick", nick);
			
			ComponentName myConnectivtyService = startService(newConn);
			assert (myConnectivtyService != null);
		
			connections.add((String)mAdapter.getItem(position));
			
			startActivity(new Intent(ActivityAndroidChatMain.this,
					ActivityChatChannel.class));
			
	}
	
	private OnClickListener mConnectListener = new OnClickListener() {
		public void onClick(View v) {
			Button opButton = (Button) findViewById(R.id.btn_ShowOpts);
			if (ServiceIRCService.state <= 0) {
				// if not started
				myConnectivtyIntent = new Intent(ActivityAndroidChatMain.this,
						ServiceIRCService.class);
				ComponentName myConnectivtyService = startService(myConnectivtyIntent);
				assert (myConnectivtyService != null);
				Button button = (Button) findViewById(R.id.btn_Connect);
				button.setText(R.string.btn_disconnect);
			
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
	
	public class ServerListAdapter extends BaseAdapter {
		
		private Context mContext;
		private Vector<String> mTitles;
		private Vector<String> mAddresses;
		
		public ServerListAdapter(Context c) {
			mContext = c;
		}
		
		public int getCount() {
			return mTitles.size();
		}
		
		public Object getItem(int position) {
			return mAddresses.elementAt(position);
		}
		
		public long getItemId(int position) {
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflate = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflate.inflate(R.layout.channel_row, parent, false);
			
			TextView title = (TextView)view.findViewById(R.id.server_title);
			TextView address = (TextView)view.findViewById(R.id.server_address);
			ImageView defaultIcon = (ImageView)view.findViewById(R.id.default_icon);
			
			title.setText(mTitles.elementAt(position));
			address.setText(mAddresses.elementAt(position));
			
			return view;
		}
		
		public void loadData() {
			ACDatabaseAdapter db = new ACDatabaseAdapter(ActivityAndroidChatMain.this);
			mTitles = db.getTitles();
			mAddresses = db.getAddresses();
			
			db.close();
			
			notifyDataSetInvalidated();
		}
	}
}
