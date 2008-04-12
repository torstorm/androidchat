package net.androidchat.client;

import java.util.Set;
import java.util.ArrayList;

import android.app.ProgressDialog;
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
	
		for(String s : userList) {
			Location loc = ServiceIRCService.temp_user_locs.get(s.toLowerCase());
			if (loc != null)
			{
	        int lat = (int) (loc.getLatitude() * 1000000);
	        int lng = (int) (loc.getLongitude() * 1000000); 
	        Point point = new Point(lat,lng);
			
		     pixelCalculator.getPointXY(point, screenCoords);
//		     canvas.drawPicture(R.drawable.dude);

		     canvas.drawCircle(screenCoords[0], screenCoords[1], 9, paint1);
			}
		}

       			
	}
	
}


