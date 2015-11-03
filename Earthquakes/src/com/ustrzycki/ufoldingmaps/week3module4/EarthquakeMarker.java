package com.ustrzycki.ufoldingmaps.week3module4;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;
import processing.core.PApplet;
/** Implements a visual marker for earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Dariusz Ustrzycki
 *
 */
public abstract class EarthquakeMarker extends SimplePointMarker
{
	
	// Did the earthquake occur on land?  This will be set by the subclasses.
	protected boolean isOnLand;

	// The radius of the Earthquake marker
	// You will want to set this in the constructor, either
	// using the thresholds below, or a continuous function
	// based on magnitude. 
	//protected float radius;
	public static final float RADIUS_MODIFIER = 4;
	
	/** Greater than or equal to this threshold is a moderate earthquake */
	public static final float THRESHOLD_MODERATE = 5;
	/** Greater than or equal to this threshold is a light earthquake */
	public static final float THRESHOLD_LIGHT = 4;

	/** Greater than or equal to this threshold is an intermediate depth */
	public static final float THRESHOLD_INTERMEDIATE = 70;
	/** Greater than or equal to this threshold is a deep depth */
	public static final float THRESHOLD_DEEP = 300;
	
	static PApplet obj = new PApplet();
	public static final int RED = obj.color(255, 0, 0); // Processing's color method to generate an int that represents a color.  
	public static final int BROWN = obj.color(171, 70, 7);
	public static final int ORANGE = obj.color(255, 153, 0);
	public static final int YELLOW = obj.color(255, 255, 0);
	public static final int BLUE = obj.color(0, 0, 255);
	public static final int WHITE = obj.color(255, 255, 255);
	public static final int BLACK = obj.color(0, 0, 0);
	
	// abstract method implemented in derived classes
	public abstract void drawEarthquake(PGraphics pg, float x, float y);
	public abstract void drawPastDay(PGraphics pg, float x, float y);	
	
	// constructor
	public EarthquakeMarker (PointFeature feature) 
	{
		super(feature.getLocation());
		// Add a radius property to the HashMap and then set the properties in the Marker
		java.util.HashMap<String, Object> properties = feature.getProperties(); //(getProperties() returns a HashMap)
		float magnitude = Float.parseFloat(properties.get("magnitude").toString()); // get method of HashMap returns V(alue)(in this case Object)
		properties.put("radius", RADIUS_MODIFIER * magnitude ); // HashMap put method associates the specified key with the specified value in this map
		setProperties(properties); //Marker interface method >> sets the properties of this marker (derived from PointFeature plus radius)
		/*System.out.println("Radius before: " + radius + " " + getRadius() + " and magnitude: " + getMagnitude());
		this.radius = 6.0f*getMagnitude();
		System.out.println("Radius after: " + radius + " " + getRadius());*/
	}
	

	// calls abstract method drawEarthquake and then checks age and draws X(inner circle/square) if needed
	public void draw(PGraphics pg, float x, float y) {
		// save previous styling
		pg.pushStyle();
			
		// determine color of marker from depth
		colorDetermine(pg);
		
		// call abstract method implemented in child class to draw markers 
		drawEarthquake(pg,  x,  y);
		
		// draw X(inner circle/square) over marker if within past day	
		if("Past Day".equals(getAge()))
			drawPastDay(pg, x, y);		
		
		// reset to previous styling
		pg.popStyle();
	}
	
	// determine color of marker from depth
	private void colorDetermine(PGraphics pg) {
		
		float depth = getDepth();
		
		if (depth < THRESHOLD_INTERMEDIATE)
			pg.fill(YELLOW); //shallow earthquake
		else if (depth < THRESHOLD_DEEP)
			pg.fill(BLUE); // intermediate
		else
			pg.fill(RED); // deep
	}
	
	/*
	 * getters for earthquake properties
	 */
	
	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
	}
	
	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());	
	}
	
	public String getTitle() {
		return (String) getProperty("title");	
	}
	
	public String getAge() {
		return (String) getProperty("age");	
	}
	
	public float getRadius() {
		return Float.parseFloat(getProperty("radius").toString());
	}
	
	public boolean isOnLand() {
		return isOnLand;
	}
	
	
}
