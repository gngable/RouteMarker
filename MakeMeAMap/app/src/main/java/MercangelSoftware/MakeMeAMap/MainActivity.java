package MercangelSoftware.MakeMeAMap;

import android.app.*;
import android.os.*;
import android.location.*;
import android.widget.*;
import android.view.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity 
{
	TextView gpsLabel;
	TextView gpsLabel2;
	TextView gpsStatusLabel;
	EditText waypointName;
	EditText routeName;
	EditText saveFileName;
	
	Location lastLocation;
	int lastStatus = -1;
	
	ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	
	HashMap<String, ArrayList<Waypoint>> routeData = new HashMap<String, ArrayList<Waypoint>>();
	//ArrayList<Waypoint> routeData = new ArrayList<Waypoint>();
	
	String currentRouteName = "";
	boolean recordingRoute = false;
	Location lastRouteLocation;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		//statusLabel = (TextView)findViewById(R.id.status_label);
		gpsLabel = (TextView)findViewById(R.id.gps_label);
		gpsLabel2 = (TextView)findViewById(R.id.gps_label2);
		gpsStatusLabel = (TextView)findViewById(R.id.gps_status_label);
		waypointName = (EditText)findViewById(R.id.waypoint_name);
		routeName = (EditText)findViewById(R.id.route_name);
		saveFileName = (EditText)findViewById(R.id.save_file_name);
		
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
								" lon: " + loc.getLongitude() +
								" alt: " + loc.getAltitude());
								
								gpsLabel2.setText("acc: " + loc.getAccuracy() +
									" bearing: " + loc.getBearing() +
									" speed: " + loc.getSpeed());
							}
					});
					
					if (recordingRoute)
					{
						if (lastRouteLocation == null || lastRouteLocation.distanceTo(loc) > 2.0)
						{
							lastRouteLocation = loc;
							
							if (!routeData.containsKey(currentRouteName))
							{
								routeData.put(currentRouteName, new ArrayList<Waypoint>());
							}

							routeData.get(currentRouteName).add(new Waypoint(currentRouteName, loc));
						}
					}
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
					
					gpsStatusLabel.setText("GPS Status: " + statustext);
					
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
	
	public void routeStartButtonClick(View view)
	{
		currentRouteName = routeName.getText().toString();
		recordingRoute = true;
		
		Toast.makeText(getApplicationContext(), "Route " + currentRouteName + " started", Toast.LENGTH_LONG).show();
	}
	
	public void routeStopButtonClick(View view)
	{
		recordingRoute = false;
		
		Toast.makeText(getApplicationContext(), "Route " + currentRouteName + " stopped", Toast.LENGTH_LONG).show();
	}
	
	public void saveButtonClick(View view)
	{
		PrintWriter writer = null;
		
		try
		{
			writer = new PrintWriter(saveFileName.getText().toString(), "UTF-8");
			
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
			writer.println("<Document><name>KmlFile</name>");

			for (Waypoint wp : waypoints)
			{
				writer.println("<Placemark>");
				writer.println("<name>" + wp.Name + "</name>");
				writer.println("<Point>");
				writer.println("<coordinates>" + wp.WaypointLocation.getLongitude() +
					"," + wp.WaypointLocation.getLatitude() +
					"," + wp.WaypointLocation.getAltitude() +
					"</coordinates>");
				writer.println("</Point>");
				writer.println("</Placemark>");
			}

			writer.println("<Style id=\"yellowLineGreenPoly\">");
			writer.println("<LineStyle>");
			writer.println("<color>7f00ffff</color>");
			writer.println("<width>4</width>");
			writer.println("</LineStyle>");
			writer.println("<PolyStyle><color>7f00ff00</color></PolyStyle>");
			writer.println("</Style>");

			// ArrayList<String> names = new ArrayList<String();

			// for (Waypoint wp : routeData)
			// {
			// 	if (names.c)
			// }

			for (String key : routeData.keySet())
			{
				writer.println("<Placemark><name>" + key + "</name>");
				writer.println("<visibility>1</visibility><styleUrl>#yellowLineGreenPoly</styleUrl>");
				writer.println("<LineString><extrude>1</extrude><tessellate>1</tessellate><altitudeMode>absolute</altitudeMode>");
        		writer.println("<coordinates>");
				
				for (Waypoint wp : routeData.get(key))
				{
					writer.println(Double.toString(wp.WaypointLocation.getLongitude()) +
						"," + wp.WaypointLocation.getLatitude() +
						"," + wp.WaypointLocation.getAltitude());
				}
				
				writer.println("</coordinates>");
				writer.println("</Placemark>");
			}

			writer.println("</Document></kml>");
		}
		catch (UnsupportedEncodingException e)
		{}
		catch (FileNotFoundException e)
		{}
		finally
		{
			if (writer != null) writer.close();
		}
	}
}
