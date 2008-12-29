package net.androidchat.client;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/** This view isn't visible when user is in a private message window. **/
public class UserList extends ListActivity {

	ClassChannelContainer mChannel;
	private UserListAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mChannel = ServiceIRCService.channels.get(ServiceIRCService.curwindow);	
		
		mAdapter = new UserListAdapter(this);
		
		//set the title to the name of the channel.
		setTitle(mChannel.channame);
		setContentView(R.layout.user_list);
		setListAdapter(mAdapter);
		
		registerForContextMenu(getListView());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
		menu.setHeaderTitle(mChannel.channame + " options");
		
		/** TODO: if the user is an operator support op commands via the menu. **/
		menu.add("Private Message");
		menu.add("Whois");
		menu.add("Ping");
		menu.add("Ignore");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getTitle().equals("Private Message"));
			privateMessageUser();
			
		return false;
	}
	
	public void privateMessageUser() {
		//TODO: implement the private message method.
	}
	
	public class UserListAdapter extends BaseAdapter {
		
		private Context mContext;
		
		public UserListAdapter(Context c) {
			mContext = c;
		}
		
		public int getCount() {
			return mChannel.chanusers.size();
		}
		
		public Object getObject(int position) {
			return mChannel.chanusers.get(position);
		}
		
		public long getItemId(int position) {
			return position;
		}
		
		public Object getItem(int position) {
			return mChannel.chanusers.get(position);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflate = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			View row = inflate.inflate(R.layout.user_list_row, parent, false);
			
			//we'll have user icons and operator icons to make life easier :)
			ImageView i = (ImageView)row.findViewById(R.id.user_list_icon);
			TextView t = (TextView)row.findViewById(R.id.user_list_name);
			
			String user = mChannel.chanusers.get(position);
			if (user.startsWith("@"))
				i.setImageResource(R.drawable.user_op_icon);
			else
				i.setImageResource(R.drawable.user_icon);
			
			if (user.startsWith("@")) t.setText(user.substring(1));
			else t.setText(user);
			
			return row;
		}
	}
}
