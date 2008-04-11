package net.androidchat.client;

import java.util.Set;
import java.util.Random;

import com.google.android.maps.Point;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
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

        
        chanGrid.setAdapter(new ChanAdapter(this, chanNames));
        chanGrid.setOnItemClickListener(this);
        //			Message.obtain(ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, "~status").sendToTarget();


	}
	
	public void onItemClick(AdapterView parent, View v, int position, long id) {
    	String chan = (String)chanGrid.obtainItem(position);
    	//ServiceIRCService.MSG_CHANGEWINDOW
    	Log.v("View change", chan);
    	Message.obtain(ServiceIRCService.ChannelViewHandler, ServiceIRCService.MSG_CHANGEWINDOW, chan).sendToTarget();

    	Message.obtain(ServiceIRCService.ChannelViewHandler, ServiceIRCService.MSG_UPDATECHAN, chan).sendToTarget();
	}
	
	
	
	public class ChanAdapter extends BaseAdapter {

		private Context mContext;
		public ChanAdapter(Context context) {
			mContext = context;
		}
		
		
        public View getView(int position, View convertView, ViewGroup parent) {

      	  
      	  TextView i = new TextView(ChannelGrid.this);

            i.setText((String)chanNames.toArray()[position]);
            i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            
            Random rand = new Random();
           int red = rand.nextInt(255+1);
           int green = rand.nextInt(255+1);
           int blue = rand.nextInt(255+1);

            i.setTextColor(Color.rgb(red,green,blue));
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
	
}