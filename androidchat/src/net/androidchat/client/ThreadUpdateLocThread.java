package net.androidchat.client;

import android.location.*;
import android.content.Context;

public class ThreadUpdateLocThread implements Runnable
{
	private Context context;
	private LocationManager lm;
	private Criteria crit;
	
	public ThreadUpdateLocThread(Context c)
	{
		context = c;
		lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		crit = new Criteria();
		crit.setAccuracy(100); // 100 meters is more than enough
		crit.setPowerRequirement(Criteria.POWER_LOW); // lets try to not waste power
	}

	public void run()
	{
		try
		{
			for (;;)
			{
				if (ServiceIRCService.state == 10) // if we're connected
				{					
					Location l = lm.getCurrentLocation((lm.getBestProvider(crit)).getName());
					ServiceIRCService.UpdateLocation(l.getLatitude(), l.getLongitude());
				}
				Thread.sleep(1000 * 60 * 1); // once a minute, when connected
			}
		} catch (InterruptedException e)
		{} catch (Exception e)
		{
			System.err.println(e.toString());
		}
	}
}
