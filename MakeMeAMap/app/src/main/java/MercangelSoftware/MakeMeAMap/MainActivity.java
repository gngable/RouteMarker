package MercangelSoftware.MakeMeAMap;

import android.app.*;
import android.os.*;
import android.location.*;
import android.widget.*;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity 
{
	TextView gpsLabel;
	EditText waypointName;
	
	Location lastLocation;
	int lastStatus = -1;
	
	ArrayList waypoints = new ArrayList();
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		//statusLabel = (TextView)findViewById(R.id.status_label);
		gpsLabel = (TextView)findViewById(R.id.gps_label);
		waypointName = (EditText)findViewById(R.id.waypoint_name);
		
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
				public void onStatusChanged(String provider, int status, Bundle bundle)
				{
					if (status == lastStatus) return;
					
					lastStatus = status;
					
					String statustext = "";
					
					if (status == LocationProvider.OUT_OF_SERVICE)
					{
						statustext = "Out of Service";
					}
					else if (status == LocationProvider.AVAILABLE)
					{
						statustext = "Available";
					}
					else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
					{
						statustext = "Temporarily Unavailable";
					}
					
					Toast.makeText(getApplicationContext(), provider + ":" + statustext, Toast.LENGTH_LONG).show();
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
	
	public void waypointButtonClick(View view)
	{
		Waypoint waypoint = new Waypoint(waypointName.getText().toString(), lastLocation);
		waypoints.add(waypoint);
		
		Toast.makeText(getApplicationContext(), "Waypoint " + waypointName.getText() + " saved", Toast.LENGTH_LONG).show();
	}
}
