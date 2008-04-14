package net.androidchat.client;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collection;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;
import java.util.Comparator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.Spinner;
import net.androidchat.client.AndroidChatOverlay;

import android.util.Log;



public class AndroidChatMap extends MapActivity implements AdapterView.OnItemSelectedListener{
	private static HashMap<String, ClassChannelDescriptor>	channel_list;
	private MapView mapView;
	private MapController mc;
	private OverlayController oc;
	
	private LocationManager lm;
	private Spinner s1;
	private Drawable mapIcon;
	private Location loc;
	private TreeSet<String> chanNames;

	private ArrayAdapter<String> adapter;
	@Override 
    public void onCreate(Bundle icicle) { 
        super.onCreate(icicle); 
		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		loc = lm.getCurrentLocation("gps");
        channel_list = ServiceIRCService.channel_list;
        DistanceComparator comp = new DistanceComparator();
    	chanNames = new TreeSet<String>(comp);
    	
    	//Set<String> tempSet = channel_list.keySet();   // 	= (TreeSet<String>)channel_list.keySet();
        chanNames.addAll(channel_list.keySet());
        setContentView(R.layout.map);

    	ImageButton button = (ImageButton) findViewById(R.id.join_chatbut);
		button.setOnClickListener(mJoinListener);
       s1 = (Spinner) findViewById(R.id.chanspinner);
       
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.addObject("Current Location");
        /*
    	setProgressBarVisibility(true);
    	int x = 1;
		while (x != 100)
		{
			setProgress(x * 10000);
			try {
				x++;
			Thread.sleep(100);
			} catch (InterruptedException IE)
			{	
			}
		}
		setProgressBarVisibility(false);
        */
        
        for(String s : chanNames) {
        	
           
            Location l = new Location();
        	l.setLatitude((float)ServiceIRCService.channel_list.get(s.toLowerCase()).loc_lat);
        	l.setLongitude((float)ServiceIRCService.channel_list.get(s.toLowerCase()).loc_lng);
        	String temp = String.format("%f, %f", l.getLatitude(), l.getLongitude());
        	Log.v("Channel loc at", temp);
        	if(l.getLatitude() != -0 && l.getLongitude() != -0) {
        	// 1 609.344 meters in a mile
        	// 1 000 meters in a km (duh)
        	
            float distance = loc.distanceTo(l);
            
            String fin = new String();
            // no user count here, should be represented by pin on map...
            fin = String.format("(%.1f mi) %s - %s", (distance/1609.344), s, ServiceIRCService.channel_list.get(s).chantopic);           
           
            adapter.addObject(fin);
        	} else {
        		String fin = new String();
                // no user count here, should be represented by pin on map...
                fin = String.format("%s - %s", s, ServiceIRCService.channel_list.get(s).chantopic);           
               
                adapter.addObject(fin);
        	}
            
            Log.v("Object test", s);
        }

        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);
        s1.setOnItemSelectedListener(this);


        mapView = (MapView)findViewById(R.id.mapv); 
        mc = mapView.getController(); 
        oc = mapView.createOverlayController();

        mapIcon = this.getResources().getDrawable(R.drawable.bubble);
        AndroidChatOverlay locOverlay = new AndroidChatOverlay(mapIcon);
        oc.add(locOverlay, true);
        
        
	    int lat = (int) (loc.getLatitude() * 1000000);
        int lng = (int) (loc.getLongitude() * 1000000); 
        Point p = new Point(lat,lng);
        if(lat != 0 && lng != 0 ) {
        //Point origin = new Point(0,0);
        //mc.animateTo(origin);
        mc.animateTo(p); 
        mc.zoomTo(9); 
        }
    } 
	
	public void onItemSelected(AdapterView parent, View v, int position, long id) {
        channel_list = ServiceIRCService.channel_list;
        chanNames.clear();
        chanNames.addAll(channel_list.keySet());

    	

		if(position == 0) {
        	 
            Location loc1 = lm.getCurrentLocation("gps");
    	    int lat = (int) (loc1.getLatitude() * 1000000);
            int lng = (int) (loc1.getLongitude() * 1000000); 
            Point p = new Point(lat,lng);
            mc.animateTo(p); 
		} else {
		
			ClassChannelDescriptor tChan = channel_list.get((String)chanNames.toArray()[position-1]);
			if(tChan.loc_lat != -0 && tChan.loc_lng != 0) {
			int lat = (int) (tChan.loc_lat * 1000000);
			int lng = (int) (tChan.loc_lng * 1000000);
            Point p = new Point(lat,lng);
            mc.animateTo(p); 
            Log.v("Selected", (String)chanNames.toArray()[position-1]);
			}
		}
	}
	
    public void onNothingSelected(AdapterView parent) {
    }
    
    public class DistanceComparator implements Comparator<String> {
     public int compare(String o1, String o2) {
    	 String loc1 = o1.toString();
    	 String loc2 = o2.toString();
    	 Location l1 = new Location();
     	l1.setLatitude((float)ServiceIRCService.channel_list.get(loc1.toLowerCase()).loc_lat);
     	l1.setLongitude((float)ServiceIRCService.channel_list.get(loc1.toLowerCase()).loc_lng);
     	
   	 Location l2 = new Location();
  	l2.setLatitude((float)ServiceIRCService.channel_list.get(loc2.toLowerCase()).loc_lat);
  	l2.setLongitude((float)ServiceIRCService.channel_list.get(loc2.toLowerCase()).loc_lng);
    float distance1 = loc.distanceTo(l1);
    float distance2 = loc.distanceTo(l2);

	if(l1.getLatitude() == -0 && l1.getLongitude() == -0) {
    	return -1;
     }
	if(l2.getLatitude() == -0 && l2.getLongitude() == -0){
		return 1;
	}
	
	if(distance1 > distance2) {
		return 1;
	} else {
		return -1;
	}
	
   }
    }
    private OnClickListener mJoinListener = new OnClickListener() {
        public void onClick(View v)
        {
            channel_list = ServiceIRCService.channel_list;

        	Set<String> chanNames = channel_list.keySet();
        	String chan = (String)chanNames.toArray()[s1.getSelectedItemPosition()-1];
        	//String chan = (String) s1.getSelectedItem();
        	if(!chan.equals("Current Location")) {
        		ServiceIRCService.JoinChan(chan);
				
        		finish();
        	}
        }
    };
    
  
}
