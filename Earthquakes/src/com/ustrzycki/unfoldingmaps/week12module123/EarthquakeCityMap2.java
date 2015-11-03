package com.ustrzycki.unfoldingmaps.week12module123;

//Java utilities libraries
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;
import java.util.Map;

//Processing library
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.AbstractMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap2 extends PApplet {

	// You can ignore this.  It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;
	private static final boolean offline = false; // IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	public static String mbTilesString = "blankLight-1-3.mbtiles"; // This is where to find the local tiles, for working without an Internet connection 
	
	public static final float THRESHOLD_GREAT = 8; // Less than this threshold is a major earthquake
	public static final float THRESHOLD_MAJOR = 7;
	public static final float THRESHOLD_STRONG = 6;
	public static final float THRESHOLD_MODERATE = 5;	
	public static final float THRESHOLD_LIGHT = 4;
	public static final float THRESHOLD_MINOR = 3;
	
	private final float baseRadius = 10;
	private final int red = color(255, 0, 0); // Processing's color method to generate an int that represents a color.  
	private final int orange = color(255, 153, 0);
	private final int yellow = color(255, 255, 0);
	private final int blue = color(0, 0, 255);
	
	// The map and feed with magnitude 2.5+ Earthquakes
	private UnfoldingMap map;
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	//parser to collect properties for each earthquake - each element of the list contains full info about a single earthquake	    
	private List<PointFeature> pointFeatures = ParseFeed.parseEarthquake(this, earthquakesURL);
	// stores markers for each city where earthquake took place
	private List<Marker> quakeMarkers = new ArrayList<Marker>();	
	
	
	public void setup() {
		size(1500, 900, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			map = new UnfoldingMap(this, 100, 10, 1500, 900, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			//earthquakesURL = "2.5_week.atom";
		}
		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
	
		// Creates a SimplePointMarker for each PointFeature earthquake from  List<PointFeature>
	    for(PointFeature pFeature : pointFeatures){
	    	SimplePointMarker simplePmarker = createMarker(pFeature);
	    	quakeMarkers.add(simplePmarker);	    	
	    	simplePmarker.setRadius(baseRadius);
	    }	    
	   	    
	    //Displays markers on the map with an UnfoldingMaps method addMarker
	    for (Marker simplePmarker : quakeMarkers) {	    	
	    	map.addMarker(simplePmarker);	
		}	 
	   
	    customizeMarkerRadius();
	    customizeMarkerColor();
	}
		
	//helper method that takes in an earthquake feature and 
	// returns a SimplePointMarker for that earthquake
	private SimplePointMarker createMarker(PointFeature feature)
	{
		//PointFeatures have a getLocation method which returns the single Location of this point feature
		return new SimplePointMarker(feature.getLocation());
	}
	
	public void draw() {
	    background(10);
	    map.draw();
	    addKey();
	}


	// helper method to draw key in GUI
	private void addKey() 
	{	
		int distanceFromLeft = 50;
		int lineSpace = 85;
				
		//Draws the text rectangle
		fill(255, 255, 255);
		rect(10, 10, 255, 350);
		
		//Draws the header text 
		fill(0, 0, 0);
		textSize(24);
		text("Earthquake key", distanceFromLeft, lineSpace/2 - 0); 
		
		//Draws the circles 
		fill(red);
		ellipse(distanceFromLeft, lineSpace*1, baseRadius*5, baseRadius*5);
		
		fill(orange);
		ellipse(distanceFromLeft, lineSpace*2, baseRadius*4, baseRadius*4);
		
		fill(yellow);
		ellipse(distanceFromLeft, lineSpace*3, baseRadius*3, baseRadius*3);
		
		fill(blue);
		ellipse(distanceFromLeft, lineSpace*4, baseRadius*2, baseRadius*2);
		
		//Draws the text next to the circles
		fill(0, 0, 0);
		textSize(15);
		int correction = 6;
		text("6.0+ Magnitude", distanceFromLeft *2, lineSpace*1 + correction); 
		text("5.0+ Magnitude", distanceFromLeft *2, lineSpace*2 + correction); 
		text("4.0+ Magnitude", distanceFromLeft *2, lineSpace*3 + correction); 
		text("Below 4.0", distanceFromLeft *2, lineSpace*4 + correction); 
	}
	
	
	// helper method to change the radius of each marker according to the magnitude of the earthquake
	private void customizeMarkerColor(){
	    
	    if (pointFeatures.size() > 0) {
	    	
	    	for (int c = 0; c < pointFeatures.size(); c++) {
				
		    	float magnitude = getMagnitude(c);
		    	
		    	if (magnitude > THRESHOLD_STRONG)
					quakeMarkers.get(c).setColor(red);
		    	else if (magnitude > THRESHOLD_MODERATE)
					quakeMarkers.get(c).setColor(orange);
				else if (magnitude > THRESHOLD_LIGHT)
					quakeMarkers.get(c).setColor(yellow);
				else 
					quakeMarkers.get(c).setColor(blue);
			}
	    }
	}
	
	private void customizeMarkerRadius(){
		
		
		if (pointFeatures.size() > 0) {

			for (int c = 0; c < pointFeatures.size(); c++) {
				
		    	float magnitude = getMagnitude(c);
		    	
		    	SimplePointMarker spMarker = (SimplePointMarker) quakeMarkers.get(c);
		    	
		    	if (magnitude > THRESHOLD_STRONG)
					spMarker.setRadius(baseRadius*4);	    	
		    	else if (magnitude > THRESHOLD_MODERATE)
					spMarker.setRadius(baseRadius*3);
				else if (magnitude > THRESHOLD_LIGHT)
					spMarker.setRadius(baseRadius*2);
				else 
					spMarker.setRadius(baseRadius);
			}
		}
		
	}
	
	private float getMagnitude(int index)
	{
		PointFeature f = pointFeatures.get(index);    	
    	Object magObj = f.getProperty("magnitude"); 
    	float magnitude = Float.parseFloat(magObj.toString());
		
		return magnitude;
	}
}




