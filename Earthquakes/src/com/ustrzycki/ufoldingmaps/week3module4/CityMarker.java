package com.ustrzycki.ufoldingmaps.week3module4;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

/** Implements a visual marker for cities on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Dariusz Ustrzycki
 *
 */
public class CityMarker extends SimplePointMarker {
	
	// The size of the triangle marker
	public static final int TRI_SIZE = 5;  
	
	// 2 constructors calling SimplePointMarker constructors
	public CityMarker(Location location) {
		super(location);
	}
	
	
	public CityMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties()); // Location and HashMap (getProperties() returns a HashMap)
	}
	
	
	// pg is the graphics object to call the graphics methods.  
	
	public void draw(PGraphics pg, float x, float y) {
		
		// Save previous drawing style
		pg.pushStyle();
		
		// draw a triangle to represent the CityMarker
		pg.fill(EarthquakeMarker.BROWN);		
		pg.triangle(x - TRI_SIZE, y + TRI_SIZE,   x + TRI_SIZE, y + TRI_SIZE, x, y - TRI_SIZE);		
		
		// Restore previous drawing style
		pg.popStyle();
	}
	
	// Local getters for some city properties. 
	public String getCity()
	{
		return getStringProperty("name");
	}
	
	public String getCountry()
	{
		return getStringProperty("country");
	}
	
	public float getPopulation()
	{
		return Float.parseFloat(getStringProperty("population"));
	}
	
}
