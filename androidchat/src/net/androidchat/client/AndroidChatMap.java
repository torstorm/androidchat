package net.androidchat.client;

import java.util.HashMap;
import java.util.Set;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;

import android.content.Context;
import android.content.Intent;
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
	private ArrayAdapter<String> adapter;
	@Override 
    public void onCreate(Bundle icicle) { 
        super.onCreate(icicle); 
        channel_list = ServiceIRCService.channel_list;
    	Set<String> chanNames = channel_list.keySet();
        
        setContentView(R.layout.map);
		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Location loc = lm.getCurrentLocation("gps");
    	ImageButton button = (ImageButton) findViewById(R.id.join_chatbut);
		button.setOnClickListener(mJoinListener);
       s1 = (Spinner) findViewById(R.id.chanspinner);
       
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.addObject("Current Location");
        for(String s : chanNames) {
        	
           
            Location l = new Location();
        	l.setLatitude((float)channel_list.get(s).loc_lat);
        	l.setLongitude((float)channel_list.get(s).loc_lng);
        	
        	// 1 609.344 meters in a mile
        	// 1 000 meters in a km (duh)
        	
            float distance = loc.distanceTo(l);
            
            String fin = new String();
            // no user count here, should be represented by pin on map...
            fin = String.format("(%.1f mi) %s - %s", (distance/1609.344), s, channel_list.get(s).chantopic);           
           
            adapter.addObject(fin);
            
            
            Log.v("Object test", s);
        }

        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);
        s1.setOnItemSelectedListener(this);


        mapView = (MapView)findViewById(R.id.mapv); 
        mc = mapView.getController(); 
        oc = mapView.createOverlayController();

        AndroidChatOverlay locOverlay = new AndroidChatOverlay();
        oc.add(locOverlay, true);
        
        
	    int lat = (int) (loc.getLatitude() * 1000000);
        int lng = (int) (loc.getLongitude() * 1000000); 
        Point p = new Point(lat,lng);
        //Point origin = new Point(0,0);
        //mc.animateTo(origin);
        mc.animateTo(p); 
        mc.zoomTo(9); 
    } 
	
	public void onItemSelected(AdapterView parent, View v, int position, long id) {
    	Set<String> chanNames = channel_list.keySet();

    	

		if(position == 0) {
        	 
            Location loc = lm.getCurrentLocation("gps");
    	    int lat = (int) (loc.getLatitude() * 1000000);
            int lng = (int) (loc.getLongitude() * 1000000); 
            Point p = new Point(lat,lng);
            mc.animateTo(p); 
		} else {
			ClassChannelDescriptor tChan = channel_list.get((String)chanNames.toArray()[position-1]);
			int lat = (int) (tChan.loc_lat * 1000000);
			int lng = (int) (tChan.loc_lng * 1000000);
            Point p = new Point(lat,lng);
            mc.animateTo(p); 
            Log.v("Selected", (String)chanNames.toArray()[position-1]);

		}
	}
	
    public void onNothingSelected(AdapterView parent) {
    }
    
    private OnClickListener mJoinListener = new OnClickListener() {
        public void onClick(View v)
        {
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
