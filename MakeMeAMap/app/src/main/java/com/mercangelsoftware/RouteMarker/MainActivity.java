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
import android.content.*;
import android.location.*;
import android.os.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import android.hardware.*;
import java.util.concurrent.*;
import android.text.*;

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
	TextView routeTimeLabel;
	TextView avgSpeedLabel;
	//EditText waypointName;
	//EditText routeName;
	//EditText saveFileName;
	Button routeStartButton;
	
	//private SensorManager sensorManager;
	
	Location lastLocation;
	int lastStatus = -1;
	
	ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	
	HashMap<String, ArrayList<Waypoint>> routeData = new HashMap<String, ArrayList<Waypoint>>();
	
	String currentRouteName = "";
	boolean recordingRoute = false;
	Location lastRouteLocation;
	double routeLength = 0.0;
	long startTime;
	
	
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
		routeTimeLabel = (TextView)findViewById(R.id.route_time_label);
		avgSpeedLabel = (TextView)findViewById(R.id.avg_speed_label);
		
		
		//waypointName = (EditText)findViewById(R.id.waypoint_name);
		//routeName = (EditText)findViewById(R.id.route_name);
		//saveFileName = (EditText)findViewById(R.id.save_file_name);
		routeStartButton = (Button)findViewById(R.id.route_start_button);
		
		LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		boolean gps_enabled = false;

		try {
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch(Exception ex) {}

		if(!gps_enabled) {
			// notify user
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setMessage("Location services are not enabled, please enable and restart this app.");
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt) {
						System.exit(0);
					}
				});
			
			dialog.show();
		}
		
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, new LocationListener(){

				@Override
				public void onLocationChanged(Location location)
				{
					final Location loc = location;
					
					lastLocation = location;
					
					String bearing = null;
                    float bear = loc.getBearing();
					if (bear != 0.0){
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
					}
					
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
								routeLength += (lastRouteLocation.distanceTo(loc));

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
								if (bearinglbl != null)
									bearingLabel.setText("Direction of travel: " + bearinglbl);
								speedLabel.setText("Speed: " + String.format("%.2f", loc.getSpeed() * 2.23694) + " mph");
								timeLabel.setText("GPS Date: " + date.toLocaleString());
								
								if (recordingRoute)
								{
									double distance = routeLength * 0.000621371;
									distanceLabel.setText(String.format("%.2f", distance));
									
									long time = (System.currentTimeMillis() - startTime);
								
									long hours = TimeUnit.MILLISECONDS.toHours(time) % 24;
									long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
									long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;

									String t = String.format("%02d:%02d:%02d",
														 hours, minutes, seconds);
								
									routeTimeLabel.setText(t);
									
									double ms = routeLength / (time / 1000);
									
									avgSpeedLabel.setText(String.format("%.2f", (ms * 2.23694)) + " mph av");
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
		
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		SensorEventListener sensorListener = new SensorEventListener(){

			private final float[] accelerometerReading = new float[3];
			private final float[] magnetometerReading = new float[3];

			private final float[] mRotationMatrix = new float[9];
			private final float[] mOrientationAngles = new float[3];
			
			@Override
			public void onSensorChanged(SensorEvent event)
			{
				if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					System.arraycopy(event.values, 0, accelerometerReading,
									 0, accelerometerReading.length);
				}
				else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					System.arraycopy(event.values, 0, magnetometerReading,
									 0, magnetometerReading.length);
									 
									 //toast("M " + mMagnetometerReading[0] + " " + mMagnetometerReading[1] + " " + mMagnetometerReading[2], true);
				}
				
				sensorManager.getRotationMatrix(mRotationMatrix, null,
												 accelerometerReading, magnetometerReading);

				// "mRotationMatrix" now has up-to-date information.

				sensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
				
				final float[] rotationMatrix = new float[9];
				sensorManager.getRotationMatrix(rotationMatrix, null,
												 accelerometerReading, magnetometerReading);

// Express the updated rotation matrix as three orientation angles.
				final float[] orientationAngles = new float[3];
				sensorManager.getOrientation(rotationMatrix, orientationAngles);
				
				
				String bearing = "";
				double bear = orientationAngles[0] * 180/Math.PI;
				
				if (bear < 0) bear = 180-bear;
				
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
				
				final String bearinglbl = bearing;
				
				gpsStatusLabel.post(new Runnable() {
						@Override
						public void run() {
							
							bearingLabel.setText("Direction of travel: " + bearinglbl);
							
						}
					});
			}

			@Override
			public void onAccuracyChanged(Sensor p1, int p2)
			{
				// TODO: Implement this method
			}

			
		};
		
		
		
		//sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
		//sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		//sensorManager.registerListener(sensorListener, Sensor.TYPE_MAGNETIC_FIELD,
			//							SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
	
		
		//sensorManager.r
    }
	
	public void waypointButtonClick(View view)
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter a name for this point of interest");

// Set up the input
		final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = input.getText().toString();
					Waypoint waypoint = new Waypoint(name, lastLocation);
					waypoints.add(waypoint);

					ArrayList<Waypoint> wpl = new ArrayList();

					wpl.add(waypoint);

					SaveKML(wpl, null, name);
				}
			});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

		builder.show();
	
		hideKeyboard(view);
	}
	
	public void routeStartButtonClick(View view)
	{
		if (routeStartButton.getText() != "Stop")
		{
			routeLength = 0.0;
			currentRouteName = "Route";
			recordingRoute = true;
			startTime = System.currentTimeMillis();
		
			routeStartButton.setText("Stop");
		
			Toast.makeText(getApplicationContext(), "Route started", Toast.LENGTH_LONG).show();
		}
		else
		{
			recordingRoute = false;
			routeStartButton.setText("Start");
			
	
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle("Name and save route?");

// Set up the input
	final EditText input = new EditText(this);

	input.setInputType(InputType.TYPE_CLASS_TEXT);
	builder.setView(input);

// Set up the buttons
	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
    @Override
    public void onClick(DialogInterface dialog, int which) {
        String name = input.getText().toString();
		
		HashMap<String, ArrayList<Waypoint>> rds = new HashMap<String, ArrayList<Waypoint>>();

		rds.put(currentRouteName, routeData.get(currentRouteName));

		SaveKML(waypoints, rds, name);
		
		routeData.clear();
		waypoints.clear();
    }
	});
	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
		routeData.clear();
		waypoints.clear();
		
    }
	});

	builder.show();
			
			
			
		}

		
	}

	private void hideKeyboard(View view)
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	private void toast(String message, boolean longtoast)
	{
		Toast.makeText(getApplicationContext(), message, (longtoast?Toast.LENGTH_LONG:Toast.LENGTH_SHORT)).show();
	}
	
	public void SaveKML(ArrayList<Waypoint> wps, HashMap<String, ArrayList<Waypoint>> rds, String name)
	{
		toast("Saving " + name, false);
		PrintWriter writer = null;
		
		String filename = name.replaceAll(" ", "_") + ".kml";
		
		String date = new Date().toLocaleString().replaceAll("/", "-").replaceAll(",", "")
		.replaceAll(" ", "_").replaceAll(":", ".");
		
		String dir ="";
		
		if (rds == null) dir = "POI/";
		
		try
		{
			if (dir != ""){
				File directory = new File(getExternalFilesDir(null) + "/" + dir);
			
				if (!directory.exists())
				{
					directory.mkdir();
				}
			}
			
			File f = new File(getExternalFilesDir(null) + "/" + dir + date + "_" + filename);
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
					writer.println("<Placemark><name>" + key + "</name><description>" + date + " " + key);
					writer.println("Total distance: " + (routeLength * 0.000621371));
					
					long time = (System.currentTimeMillis() - startTime);

					long hours = TimeUnit.MILLISECONDS.toHours(time) % 24;
					long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
					long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;
					
					writer.println("Total time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
					
					double ms = routeLength / (time / 1000);
			
					writer.println("Average speed: " + String.format("%.2f", (ms * 2.23694)) + " mph av");
				    writer.println("</description>");
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
		
		showDialog("Save complete " + getExternalFilesDir(null) + "/" + dir + date + "_" + filename);
	}
	
	public void aboutButtonClick(View view)
	{
		//AlertDialog.Builder builder = new AlertDialog.Builder(this);
		showDialog("Created by Nick Gable (Mercangel Software)" + 
						   "\nFiles saved to " + getExternalFilesDir(null));
		
		//toast("Created by Nick Gable (Mercangel Software)", true);
	}
	
	public void showDialog(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.show();
	}
}
