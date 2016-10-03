package MercangelSoftware.MakeMeAMap;

import android.app.*;
import android.os.*;
import android.location.*;
import android.widget.*;

public class MainActivity extends Activity 
{
	TextView statusLabel;
	TextView gpsLabel;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		statusLabel = (TextView)findViewById(R.id.status_label);
		gpsLabel = (TextView)findViewById(R.id.gps_label);
		
		LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener(){

				@Override
				public void onLocationChanged(Location location)
				{
					final Location loc = location;
					gpsLabel.post(new Runnable(){
							@Override
							public void run()
							{
								gpsLabel.setText("lat: " + loc.getLatitude() +
								" lon: " + loc.getLongitude());
							}
					});
				}

				@Override
				public void onStatusChanged(String p1, int p2, Bundle p3)
				{
					// TODO: Implement this method
				}

				@Override
				public void onProviderEnabled(String p1)
				{
					// TODO: Implement this method
				}

				@Override
				public void onProviderDisabled(String p1)
				{
					// TODO: Implement this method
				}

			
		});
    }
}
