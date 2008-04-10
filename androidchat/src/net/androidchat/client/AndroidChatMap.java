package net.androidchat.client;

import java.util.HashMap;
import java.util.Set;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import net.androidchat.client.AndroidChatOverlay;

import android.util.Log;

public class AndroidChatMap extends MapActivity {
	private static HashMap<String, ClassChannelDescriptor>	channel_list;
	private MapView mapView;
	private MapController mc;
	private OverlayController oc;
	
	private LocationManager lm;

	@Override 
    public void onCreate(Bundle icicle) { 
        super.onCreate(icicle); 
        channel_list = ServiceIRCService.channel_list;
    	Set<String> chanNames = channel_list.keySet();
        
        

        
        setContentView(R.layout.map); 
        Spinner s1 = (Spinner) findViewById(R.id.chanspinner);
       
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        for(int i = 0; i < chanNames.size(); i++) {
            adapter.addObject((String)chanNames.toArray()[i]);
            Log.v("Object test", (String)chanNames.toArray()[i]);
        }

        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);

		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        mapView = (MapView)findViewById(R.id.mapv); 
        mc = mapView.getController(); 
        oc = mapView.createOverlayController();

        AndroidChatOverlay locOverlay = new AndroidChatOverlay();
        oc.add(locOverlay, true);
        
        Location loc = lm.getCurrentLocation("gps");
	    int lat = (int) (loc.getLatitude() * 1000000);
        int lng = (int) (loc.getLongitude() * 1000000); 
        Point p = new Point(lat,lng);
        //Point origin = new Point(0,0);
        //mc.animateTo(origin);
        mc.animateTo(p); 
        mc.zoomTo(9); 
    } 
	
}
