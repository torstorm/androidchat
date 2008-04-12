package net.androidchat.client;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import java.util.ArrayList;
import java.util.Set;
import java.lang.Thread;
import android.widget.Spinner;
import net.androidchat.client.AndroidChatOverlay;


public class ActivityUserMap extends MapActivity implements AdapterView.OnItemSelectedListener{
	private String chanName;
	private ArrayList<String> userList;
	private MapView mapView;
	private MapController mc;
	private OverlayController oc;
	
	private LocationManager lm;
	private Spinner s1;
	
	@Override 
    public void onCreate(Bundle icicle) { 
        super.onCreate(icicle); 
        
		// this is wrong chanName = icicle.getString("name");

        chanName = ServiceIRCService.curwindow;
        
        setContentView(R.layout.usermap);
        
		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Location loc = lm.getCurrentLocation("gps");
    //	ImageButton button = (ImageButton) findViewById(R.id.msg_userbut);
		//button.setOnClickListener(mMsgListener);
        s1 = (Spinner) findViewById(R.id.userspinner);

        userList = ServiceIRCService.channels.get(chanName).chanusers;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.addObject("Current Location");
     
        for(String s : userList) {
        	adapter.addObject(s);
        }
        
        s1.setAdapter(adapter);
        s1.setOnItemSelectedListener(this);
        
        mapView = (MapView)findViewById(R.id.umapv); 
        mc = mapView.getController(); 
        oc = mapView.createOverlayController();


		ServiceIRCService.temp_user_locs.clear();
		for(String s : userList)
			ServiceIRCService.RequestUserLocation(s);

		ProgressDialog pg = ProgressDialog.show(ServiceIRCService.context, ServiceIRCService.context.getText(R.string.app_name), ServiceIRCService.context.getText(R.string.ui_progress));
		
		while (ServiceIRCService.temp_user_locs.size() != userList.size())
		{
			pg.setProgress((int)(((float)(ServiceIRCService.temp_user_locs.size()) / (float)userList.size()) * 10000));
			try {
			Thread.sleep(100);
			} catch (InterruptedException IE)
			{	
			}
		}
		pg.dismiss();
        
        userMapOverlay locOverlay = new userMapOverlay(chanName);
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
    	//Set<String> chanNames = channel_list.keySet();

    	

		if(position == 0) {
        	 
            Location loc = lm.getCurrentLocation("gps");
    	    int lat = (int) (loc.getLatitude() * 1000000);
            int lng = (int) (loc.getLongitude() * 1000000); 
            Point p = new Point(lat,lng);
            mc.animateTo(p); 
		} else {
		//	ClassChannelDescriptor tChan = channel_list.get((String)chanNames.toArray()[position-1]);
			ServiceIRCService.RequestUserLocation((String)parent.obtainItem(position-1));

			ServiceIRCService.temp_user_locs.clear();
			while(!ServiceIRCService.temp_user_locs.containsKey((String)parent.obtainItem(position-1))) {
				//Thread.sleep(100);
			}
			Location tUser = ServiceIRCService.temp_user_locs.get((String)parent.obtainItem(position-1));
			
			int lat = (int) (tUser.getLatitude() * 1000000);
			int lng = (int) (tUser.getLongitude() * 1000000);
            Point p = new Point(lat,lng);
            mc.animateTo(p); 

		}
	}
	
    public void onNothingSelected(AdapterView parent) {
    }
    
    
}
