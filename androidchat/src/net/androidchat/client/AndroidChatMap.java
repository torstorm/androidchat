package net.androidchat.client;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.Point;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;


public class AndroidChatMap extends MapActivity {
	private String[][] chanLocs;
	private MapView mapView;
	private MapController mc;
	
	private LocationManager lm;

	@Override 
    public void onCreate(Bundle icicle) { 
        super.onCreate(icicle); 
        setContentView(R.layout.map); 

		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        mapView = (MapView)findViewById(R.id.mapv); 
        mc = mapView.getController(); 
        Location loc = lm.getCurrentLocation("gps");
	    int lat = (int) (loc.getLatitude() * 1000000);
        int lng = (int) (loc.getLongitude() * 1000000); 
        Point p = new Point(lat,lng);
        mc.animateTo(p);
        //mc.animateTo(p); 
        mc.zoomTo(9); 
    } 
	
}
