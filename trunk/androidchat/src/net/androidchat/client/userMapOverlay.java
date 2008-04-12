package net.androidchat.client;

import java.util.Set;
import java.util.ArrayList;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Address;
import android.util.Log;
import android.location.Location;

import com.google.android.maps.Point;
import com.google.android.maps.Overlay;
import com.google.android.maps.Overlay.PixelCalculator;

public class userMapOverlay extends Overlay { 
	Paint paint1 = new Paint();
	String chanName;
	ArrayList<String> userList;
	
	userMapOverlay(String chan) {
		chanName = chan;
		userList = ServiceIRCService.channels.get(chanName).chanusers;
	}

	public void draw(Canvas canvas, PixelCalculator pixelCalculator, boolean b) {
		super.draw(canvas, pixelCalculator, b);
        int[] screenCoords = new int[2];


		ServiceIRCService.temp_user_locs.clear();
		for(String s : userList)
		{
		ServiceIRCService.RequestUserLocation(s);
		}

		while (ServiceIRCService.temp_user_locs.size() != userList.size())
		{
		}


		
		for(int i = 0; i < userList.size(); i++) {
			Location loc = ServiceIRCService.temp_user_locs.get(userList.get(i));
	        int lat = (int) (loc.getLatitude() * 1000000);
	        int lng = (int) (loc.getLongitude() * 1000000); 
	        Point point = new Point(lat,lng);
			
		     pixelCalculator.getPointXY(point, screenCoords);
		     canvas.drawCircle(screenCoords[0], screenCoords[1], 9, paint1);
		}

       			
	}
	
}


