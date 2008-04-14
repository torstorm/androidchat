package net.androidchat.client;

import java.util.Set;
import java.util.Random;

import com.google.android.maps.Point;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class ChannelGrid extends Activity implements AdapterView.OnItemClickListener{
    GridView chanGrid;
	Set<String> chanNames;


	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.chan_grid);
		
        chanNames = ServiceIRCService.channels.keySet();

        chanGrid = (GridView) findViewById(R.id.cGrid);

        
        chanGrid.setAdapter(new ChanAdapter(this));
        chanGrid.setOnItemClickListener(this);
        this.setTitle("Choose which window to swap to:");
        //			Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, "~status").sendToTarget();


	}
	
	public void onItemClick(AdapterView parent, View v, int position, long id) {
    	String chan = (String)chanGrid.obtainItem(position);
    	//ServiceIRCService.MSG_CHANGEWINDOW
    	Log.v("View change", chan);
    	Message.obtain(ServiceIRCService.ChannelViewHandler, ServiceIRCService.MSG_CHANGEWINDOW, chan).sendToTarget();
    	Message.obtain(ServiceIRCService.ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, chan).sendToTarget();
    	finish();
    	
	}
	
	
	
	public class ChanAdapter extends BaseAdapter {

		private Context mContext;
		public ChanAdapter(Context context) {
			mContext = context;
		}
		
		
        public View getView(int position, View convertView, ViewGroup parent) {
            chanNames = ServiceIRCService.channels.keySet();

      	  
      	  TextView i = new TextView(ChannelGrid.this);

            i.setText((String)chanNames.toArray()[position]);
            
  
            i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            
            Random rand = new Random();
           int red = rand.nextInt(127+1) + 128;
           int green = rand.nextInt(127+1) + 128;
           int blue = rand.nextInt(127+1) + 128;

           if(ServiceIRCService.curwindow.equals((String)chanNames.toArray()[position])) {
        	   i.setTypeface(Typeface.DEFAULT_BOLD_ITALIC);
           }
            i.setTextColor(Color.rgb(red,green,blue));
            i.setBackgroundColor(0x33FFFFFF);
            i.setPadding(3, 3, 3, 3);
            
         //   ResolveInfo info = mApps.get(position);

        //    i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
          //  i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //i.setLayoutParams(new Gallery.LayoutParams(50, 50));
            return i;
        }


        public final int getCount() {
            return chanNames.size();
        }

        public final Object getItem(int position) {
            return chanNames.toArray()[position];
        }

        public final long getItemId(int position) {
            return position;
        }

	}
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        super.onCreateOptionsMenu(menu);
	        
	        // Parameters for menu.add are:
	        // group -- Not used here.
	        // id -- Used only when you want to handle and identify the click yourself.
	        // title
	        menu.add(0, 0, "Close Current Window"); 

	        
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
	        	//ServiceIRCService.curwindow
	        	ServiceIRCService.SendToChan(ServiceIRCService.curwindow, "/close");
	        	finish();
	            return true;
	        }
	        return false;
	    }

	
}
