package com.mercangelsoftware.RouteMarker;
import android.location.*;

public class Waypoint
{
	public String Name;
	//public Location WaypointLocation;
	public double Longitude;
	public double Latitude;
	public double Altitude;
	
	public Waypoint(String name, Location location)
	{
		this.Name = name;
		this.Longitude = location.getLongitude();
		this.Latitude = location.getLatitude();
		this.Altitude = location.getAltitude();
	}
}
