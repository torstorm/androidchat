package net.androidchat.client;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import net.androidchat.client.ActivityChatChannel;
import android.view.Window;

public class ActivityUserMap extends MapActivity implements AdapterView.OnItemSelectedListener{
	private String chanName;
	private ArrayList<String> userList;
	private MapView mapView;
	private MapController mc;
	private OverlayController oc;
	
	private LocationManager lm;
	private Spinner s1;
	private Drawable icon;
	
	@Override 
    public void onCreate(Bundle icicle) { 
        super.onCreate(icicle); 
        requestWindowFeature(Window.FEATURE_PROGRESS);
        
        chanName = ServiceIRCService.curwindow;

        String titleStr = new String();
        titleStr = String.format("AndroidChat - Users on %s", chanName);
        this.setTitle(titleStr);
		// this is wrong chanName = icicle.getString("name");

        
        setContentView(R.layout.usermap);
        
		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Location loc = lm.getCurrentLocation("gps");
    //	ImageButton button = (ImageButton) findViewById(R.id.msg_userbut);
		//button.setOnClickListener(mMsgListener);
		ImageButton button = (ImageButton) findViewById(R.id.msg_user);
		button.setOnClickListener(mPMListener);
        s1 = (Spinner) findViewById(R.id.userspinner);

        userList = ServiceIRCService.channels.get(chanName).chanusers;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.addObject("Current Location");

        s1.setAdapter(adapter);
        s1.setOnItemSelectedListener(this);
        
        mapView = (MapView)findViewById(R.id.umapv); 
        mc = mapView.getController(); 
        oc = mapView.createOverlayController();


		ServiceIRCService.temp_user_locs.clear();
		for(String s : userList)
			ServiceIRCService.RequestUserLocation(s);
	
		setProgressBarVisibility(true);
		while (ServiceIRCService.temp_user_locs.size() != userList.size())
		{
			setProgress((int)(((float)(ServiceIRCService.temp_user_locs.size()) / (float)userList.size()) * 10000));
			try {
			Thread.sleep(100);
			} catch (InterruptedException IE)
			{	
			}
		}
		setProgressBarVisibility(false);
		

        for(String s : userList) {
        	String fin = new String();
        	Location loca = ServiceIRCService.temp_user_locs.get(s.toLowerCase());
            float distance = loca.distanceTo(loc);
            if(loca.getLatitude() != 0 && loca.getLatitude() != 0) {
            fin = String.format("(%.1f mi) %s",(distance/1609.344), s);           
            } else {
            	fin = String.format("%s", s);
            }
            adapter.addObject(fin);
        }

        icon = this.getResources().getDrawable(R.drawable.dude);
        userMapOverlay locOverlay = new userMapOverlay(chanName, icon);
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
		if(position == 0) {
        	 
            Location loc = lm.getCurrentLocation("gps");
    	    int lat = (int) (loc.getLatitude() * 1000000);
            int lng = (int) (loc.getLongitude() * 1000000); 
            Point p = new Point(lat,lng);
            mc.animateTo(p); 
		} else {
			
//			ServiceIRCService.RequestUserLocation((String)parent.obtainItem(position-1));
//
//			ServiceIRCService.temp_user_locs.clear();
//			while(!ServiceIRCService.temp_user_locs.containsKey((String)parent.obtainItem(position-1))) {
//				//Thread.sleep(100);
//			}
			//userList
			String key = userList.get(position-1);
			if (ServiceIRCService.temp_user_locs.containsKey(key.toLowerCase()))
			{
				Location tUser = ServiceIRCService.temp_user_locs.get(key.toLowerCase());
			
			int lat = (int) (tUser.getLatitude() * 1000000);
			int lng = (int) (tUser.getLongitude() * 1000000);
			if(lat != 0 && lng != 0 ) {
            Point p = new Point(lat,lng);
            mc.animateTo(p); 
			}
			}
		}
	}
	
    public void onNothingSelected(AdapterView parent) {
    }
    
    private OnClickListener mPMListener = new OnClickListener() {
        public void onClick(View v)
        {
            //userList = ServiceIRCService.channels.get(chanName).chanusers;

        	String user = (String)userList.toArray()[s1.getSelectedItemPosition()-1];
        	//String chan = (String) s1.getSelectedItem();
        	if(!user.equals("Current Location")) {
        		ServiceIRCService.OpenPMWindow(user);
				
        		finish();
        	}
        }
    };
}
