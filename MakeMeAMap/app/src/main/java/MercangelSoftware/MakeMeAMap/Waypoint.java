package MercangelSoftware.MakeMeAMap;
import android.location.*;

public class Waypoint
{
	public String Name;
	public Location WaypointLocation;
	
	public Waypoint(String name, Location location)
	{
		this.Name = name;
		this.WaypointLocation = location;
	}
}
