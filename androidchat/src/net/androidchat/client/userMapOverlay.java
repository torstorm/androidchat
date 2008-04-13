package net.androidchat.client;

import java.util.Set;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
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
    BitmapDrawable mapIcon; 
    static int w,h;
    
	userMapOverlay(String chan, Drawable icon) {
		chanName = chan;
		userList = ServiceIRCService.channels.get(chanName).chanusers;	
        mapIcon = (BitmapDrawable) icon; 
        w = mapIcon.getIntrinsicWidth();
        h = mapIcon.getIntrinsicHeight();
	}

	public void draw(Canvas canvas, PixelCalculator pixelCalculator, boolean b) {
		super.draw(canvas, pixelCalculator, b);
        int[] screenCoords = new int[2];
	
		for(String s : userList) {
			Location loc = ServiceIRCService.temp_user_locs.get(s.toLowerCase());
			if (loc != null)
			{
				if(loc.getLatitude() != 0 && loc.getLongitude() != 0 ) {
	        int lat = (int) (loc.getLatitude() * 1000000);
	        int lng = (int) (loc.getLongitude() * 1000000); 
	        Point point = new Point(lat,lng);
			
		     pixelCalculator.getPointXY(point, screenCoords);

		     mapIcon.setBounds(screenCoords[0] - w / 2, screenCoords[1] - h, 
                     screenCoords[0] + w / 2, screenCoords[1]); 
             //mapIcon.setAlpha(70); 
             mapIcon.draw(canvas); 
				}
			}
		}

       			
	}
	
}


