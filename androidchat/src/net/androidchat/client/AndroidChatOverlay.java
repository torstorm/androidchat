package net.androidchat.client;

import java.util.HashMap;
import java.util.Set;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable; 
import android.graphics.drawable.Drawable;
import android.location.Address;
import com.google.android.maps.Point;
import com.google.android.maps.Overlay;
import com.google.android.maps.Overlay.PixelCalculator;

import android.util.Log;


public class AndroidChatOverlay extends Overlay {
	private static HashMap<String, ClassChannelDescriptor>	channel_list;
	Paint paint1 = new Paint();
    BitmapDrawable mapIcon; 
    static int w,h;
	
	AndroidChatOverlay(Drawable icon) {
		channel_list = ServiceIRCService.channel_list;
        mapIcon = (BitmapDrawable) icon; 
        w = mapIcon.getIntrinsicWidth();
        h = mapIcon.getIntrinsicHeight();
	}

	public void draw(Canvas canvas, PixelCalculator pixelCalculator, boolean b) {
		super.draw(canvas, pixelCalculator, b);
        int[] screenCoords = new int[2];

		Set<String> chanSet = channel_list.keySet();
		ClassChannelDescriptor temp;

		for(int i = 0; i < chanSet.size(); i++) {
			Log.v("Test Chat", (String)chanSet.toArray()[i]);
			temp = channel_list.get(chanSet.toArray()[i]);
			StringBuffer test = new StringBuffer();
			StringBuffer test2 = new StringBuffer();
			test.append(temp.loc_lat);
			test2.append(temp.loc_lng);
			Log.v("Test lat", test.toString());
			Log.v("Test lng", test2.toString());

			 int lat = (int) (temp.loc_lat * 1000000); 
		     int lng = (int)(temp.loc_lng * 1000000); 
		     Point point = new Point(lat,lng);
		     pixelCalculator.getPointXY(point, screenCoords);

             mapIcon.setBounds(screenCoords[0] - w / 2, screenCoords[1] - h, 
                     screenCoords[0] + w / 2, screenCoords[1]); 
             //mapIcon.setAlpha(70); 
             mapIcon.draw(canvas); 
		}

       			
	}
	
}
