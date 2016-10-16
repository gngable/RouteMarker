// MIT License

// Copyright (c) 2016 Nick Gable (Mercangel Software)

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.mercangelsoftware.RouteMarker;

import android.app.*;
import android.os.*;
import android.location.*;
import android.widget.*;
import android.view.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity 
{
	TextView longitudeLabel;
	TextView latitudeLabel;
	TextView altitudeLabel;
	TextView accuracyLabel;
	TextView bearingLabel;
	TextView speedLabel;
	TextView timeLabel;
	TextView gpsStatusLabel;
	TextView distanceLabel;
	EditText waypointName;
	EditText routeName;
	EditText saveFileName;
	Button routeStartButton;
	
	Location lastLocation;
	int lastStatus = -1;
	
	ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	
	HashMap<String, ArrayList<Waypoint>> routeData = new HashMap<String, ArrayList<Waypoint>>();
	
	String currentRouteName = "";
	boolean recordingRoute = false;
	Location lastRouteLocation;
	double routeLength = 0.0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		//statusLabel = (TextView)findViewById(R.id.status_label);
		longitudeLabel = (TextView)findViewById(R.id.longitude_label);
		latitudeLabel = (TextView)findViewById(R.id.latitude_label);
		altitudeLabel = (TextView)findViewById(R.id.altitude_label);
		bearingLabel = (TextView)findViewById(R.id.bearing_label);
		accuracyLabel = (TextView)findViewById(R.id.accuracy_label);
		speedLabel = (TextView)findViewById(R.id.speed_label);
		timeLabel = (TextView)findViewById(R.id.time_label);
		gpsStatusLabel = (TextView)findViewById(R.id.gps_status_label);
		distanceLabel = (TextView)findViewById(R.id.distance_label);
		waypointName = (EditText)findViewById(R.id.waypoint_name);
		routeName = (EditText)findViewById(R.id.route_name);
		saveFileName
 = (EditText)findViewById(R.id.save_file_name);
		routeStartButton = (Button)findViewById(R.id.route_start_button);
		
		LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, new LocationListener(){

				@Override
				public void onLocationChanged(Location location)
				{
					final Location loc = location;
					
					lastLocation = location;
					
					String bearing = "";
                    float bear = loc.getBearing();
                    float fourtyfivehalf = 45 / 2;

                    if (bear <= fourtyfivehalf && bear > -fourtyfivehalf) {
                        bearing = "N";
                    } else if (bear <= 45 + fourtyfivehalf && bear > 45 - fourtyfivehalf) {
                        bearing = "NE";
                    } else if (bear <= 90 + fourtyfivehalf && bear > 90 - fourtyfivehalf) {
                        bearing = "E";
                    } else if (bear <= 135 + fourtyfivehalf && bear > 135 - fourtyfivehalf) {
                        bearing = "SE";
                    } else if (bear <= 180 + fourtyfivehalf && bear > 180 - fourtyfivehalf) {
                        bearing = "S";
                    } else if (bear <= 225 + fourtyfivehalf && bear > 225 - fourtyfivehalf) {
                        bearing = "SW";
                    } else if (bear <= 270 + fourtyfivehalf && bear > 270 - fourtyfivehalf) {
                        bearing = "W";
                    } else if (bear <= 315 + fourtyfivehalf && bear > 315 - fourtyfivehalf) {
                        bearing = "NW";
                    }

                    bearing += " (" + bear + ")";
					
					double acc = location.getAccuracy();
                    String accuracy = "";
                    if (acc <= 3.0) {
                        accuracy = "Excellent";
                    } else if (acc <= 6) {
                        accuracy = "Good";
                    } else if (acc < 10) {
                        accuracy = "Fair";
                    } else {
                        accuracy = "Poor";
                    }

                    accuracy += " (" + String.format("%.2f", acc * 3.28084) + "ft)";
					
					final String bearinglbl = bearing;
                    final String accuracylbl = accuracy;
					final Date date = new Date(loc.getTime());
					
					if (recordingRoute)
					{
						if (lastRouteLocation == null || lastRouteLocation.distanceTo(loc) > 1.0)
						{
							if (!routeData.containsKey(currentRouteName))
							{
								routeData.put(currentRouteName, new ArrayList<Waypoint>());
							}

							routeData.get(currentRouteName).add(new Waypoint(currentRouteName, loc));
							
							if (lastRouteLocation != null)
								routeLength += (lastRouteLocation.distanceTo(loc) * 3.28084);

							lastRouteLocation = loc;
						}
					}
					
					gpsStatusLabel.post(new Runnable() {
							@Override
							public void run() {
								latitudeLabel.setText("Latitude: " + loc.getLatitude());
								longitudeLabel.setText("Longitude: " + loc.getLongitude());
								altitudeLabel.setText("Altitude: " + String.format("%.2f", loc.getAltitude() * 3.28084) + "ft");

								accuracyLabel.setText("Accuracy: " + accuracylbl);
								bearingLabel.setText("Bearing: " + bearinglbl);
								speedLabel.setText("Speed: " + String.format("%.2f", loc.getSpeed() * 2.23694) + " mph");
								timeLabel.setText("Date: " + date.toLocaleString());
								
								if (recordingRoute)
								{
									distanceLabel.setText(currentRouteName + " length " + String.format("%.2f", routeLength) + "ft");
								}
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
					
					gpsStatusLabel.setText("GPS Status: " + statustext);
					
					//Toast.makeText(getApplicationContext(), provider + ":" + statustext, Toast.LENGTH_LONG).show();
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
		
		ArrayList<Waypoint> wpl = new ArrayList();
		
		wpl.add(waypoint);
		
		SaveKML(wpl, null, waypointName.getText().toString());

		waypointName.clearFocus();
		routeName.clearFocus();
		saveFileName.clearFocus();
	}
	
	public void routeStartButtonClick(View view)
	{
		if (routeStartButton.getText() != "Pause")
		{
			routeLength = 0.0;
			currentRouteName = routeName.getText().toString();
			recordingRoute = true;
		
			routeStartButton.setText("Pause");
		
			Toast.makeText(getApplicationContext(), "Route " + currentRouteName + " started", Toast.LENGTH_LONG).show();
		}
		else
		{
			recordingRoute = false;
			routeStartButton.setText("Resume");
		}

		waypointName.clearFocus();
		routeName.clearFocus();
		saveFileName.clearFocus();
	}
	
	public void routeStopButtonClick(View view)
	{
		recordingRoute = false;
		
		routeStartButton.setText("Start");
		
		HashMap<String, ArrayList<Waypoint>> rds = new HashMap<String, ArrayList<Waypoint>>();
		
		rds.put(currentRouteName, routeData.get(currentRouteName));
		
		SaveKML(null, rds, currentRouteName);

		waypointName.clearFocus();
		routeName.clearFocus();
		saveFileName.clearFocus();
	}
	
	private void toast(String message, boolean longtoast)
	{
		Toast.makeText(getApplicationContext(), message, (longtoast?Toast.LENGTH_LONG:Toast.LENGTH_SHORT)).show();
	}
	
	public void saveButtonClick(View view)
	{
		SaveKML(waypoints, routeData, saveFileName.getText().toString());
		waypoints.clear();
		routeData.clear();

		waypointName.clearFocus();
		routeName.clearFocus();
		saveFileName.clearFocus();
	}
	
	public void SaveKML(ArrayList<Waypoint> wps, HashMap<String, ArrayList<Waypoint>> rds, String name)
	{
		toast("Saving " + name, false);
		PrintWriter writer = null;
		
		String filename = name.replaceAll(" ", "_") + ".kml";
		
		String date = new Date().toLocaleString().replaceAll("/", "-").replaceAll(",", "")
		.replaceAll(" ", "_").replaceAll(":", ".");
		
		try
		{
			//File dir = new File(getExternalFilesDir(null) + "/" + date);
			
			//if (!dir.exists())
			//{
				//dir.mkdir();
			//}
			
			File f = new File(getExternalFilesDir(null) + "/" + date + "_" + filename);
			writer = new PrintWriter(f, "UTF-8");
			
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
			writer.println("<Document><name>" + name + "</name>");
			
			if (wps != null && wps.size() > 0)
			{
				for (Waypoint wp : wps)
				{
					writer.println("<Placemark>");
					writer.println("<name>" + wp.Name + "</name>");
					writer.println("<description>" + date + " " + wp.Name + "</description>");
					writer.println("<Point>");
					writer.println("<coordinates>" + wp.Longitude +
						"," + wp.Latitude +
						"," + wp.Altitude +
						"</coordinates>");
					writer.println("</Point>");
					writer.println("</Placemark>");
				}
			}

			writer.println("<Style id=\"yellowLineGreenPoly\">");
			writer.println("<LineStyle>");
			writer.println("<color>7f00ffff</color>");
			writer.println("<width>4</width>");
			writer.println("</LineStyle>");
			writer.println("<PolyStyle><color>7f00ff00</color></PolyStyle>");
			writer.println("</Style>");

			if (rds != null && rds.size() > 0)
			{
				for (String key : rds.keySet())
				{
					writer.println("<Placemark><name>" + key + "</name><description>" + date + " " + key + "</description>");
					writer.println("<visibility>1</visibility><styleUrl>#yellowLineGreenPoly</styleUrl>");
					writer.println("<LineString><extrude>1</extrude><tessellate>1</tessellate><altitudeMode>clampToGround</altitudeMode>");
        			writer.println("<coordinates>");
				
					for (Waypoint wp : rds.get(key))
					{
						writer.println(Double.toString(wp.Longitude) +
							"," + wp.Latitude +
							"," + wp.Altitude);
					}
				
					writer.println("</coordinates></LineString>");
					writer.println("</Placemark>");
				}
			}

			writer.println("</Document></kml>");
		}
		catch (UnsupportedEncodingException e)
		{
			toast("1 " + e.getMessage(), true);
			return;
		}
		catch (FileNotFoundException e)
		{
			toast("2 " + e.getMessage(), true);
			return;
		}
		catch (Exception e)
		{
			toast("3 " + e.getMessage(), true);
			return;
		}
		finally
		{
			if (writer != null) writer.close();
		}
		
		toast("Save complete " + getExternalFilesDir(null) + "/" + date + "_" + filename, true);
	}
	
	public void aboutButtonClick(View view)
	{
		toast("Created by Nick Gable (Mercangel Software)", true);
	}
}
