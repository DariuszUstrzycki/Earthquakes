package com.ustrzycki.ufoldingmaps.week3module4;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Dariusz Ustrzycki
 * Date: October 15, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city (each marker contains name of city population, coastal(true/false), point coordinates)
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;
	//Markers for each country
	private List<Marker> countryMarkers;	
	// number of earthquakes per each country from countryMarkers
	private int[] countryQuakes;
	
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(1500, 900, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 100, 10, 1500, 900, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		//earthquakesURL = "test1.atom";
		// earthquakesURL = "test2.atom";
		
		// WHEN TAKING THIS QUIZ: Uncomment the next line
	    //earthquakesURL = "quiz1.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile); // read in name of city population, coastal(true/false), point coordinates
		cityMarkers = new ArrayList<Marker>();
		// create a CityMarker for each Feature and add it to the cityMarkers 
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL); // each PointFeature stores earthquakes's magnitude, time, location, date
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake and then create and add to quakeMarkers an appropriate LandQuakeMarker or OceanQuakeMarker containing the properties of the earthquake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));		    
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }
	   
	    // could be used for debugging
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	    
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		// Processing's graphics methods here
		fill(255, 250, 240);
		rect(25, 50, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 75);
		
		int x = 60;
		int y = 100;		
		fill(EarthquakeMarker.BROWN);
		triangle(x - CityMarker.TRI_SIZE, y + CityMarker.TRI_SIZE,   x + CityMarker.TRI_SIZE, y + CityMarker.TRI_SIZE, x, y - CityMarker.TRI_SIZE);	
		
		fill(EarthquakeMarker.WHITE);
		ellipse(x, y + 25, CityMarker.TRI_SIZE * 2, CityMarker.TRI_SIZE * 2);
		rect(x - CityMarker.TRI_SIZE, y + 47,  CityMarker.TRI_SIZE * 2, CityMarker.TRI_SIZE * 2);
		
		fill(EarthquakeMarker.YELLOW);
		ellipse(x, y + 100, CityMarker.TRI_SIZE * 2, CityMarker.TRI_SIZE * 2);
		fill(EarthquakeMarker.BLUE);
		ellipse(x, y + 125, CityMarker.TRI_SIZE * 2, CityMarker.TRI_SIZE * 2);
		fill(EarthquakeMarker.RED);
		ellipse(x, y + 150, CityMarker.TRI_SIZE * 2, CityMarker.TRI_SIZE * 2);
		
		fill(EarthquakeMarker.WHITE);
		ellipse(x, y + 175, CityMarker.TRI_SIZE * 2, CityMarker.TRI_SIZE * 2);
		fill(EarthquakeMarker.BLACK);
		ellipse(x, y + 175, CityMarker.TRI_SIZE , CityMarker.TRI_SIZE );
		
		fill(EarthquakeMarker.WHITE);
		rect(x - CityMarker.TRI_SIZE + 23, y + 170,  CityMarker.TRI_SIZE * 2 + 1, CityMarker.TRI_SIZE * 2 + 1);
		fill(EarthquakeMarker.BLACK);
		rect(x - CityMarker.TRI_SIZE + 26, y + 173,  CityMarker.TRI_SIZE, CityMarker.TRI_SIZE);
		
		fill(EarthquakeMarker.BLACK);
		text("City Marker", x + 15, y);		
		text("Land Quake", x + 15, y + 25);
		text("Ocean Quake", x + 15, y + 50);
		text("Size - Magnitude", 50, y + 75);
		text("Shallow", x + 15, y + 100);	
		text("Intermediate", x + 15, y + 125);
		text("Deep", x + 15, y + 150);
		text("Past day", x + 35, y + 173);
		
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {
		
		// loop over all countries to check if location is in any of them		
		for(Marker country : countryMarkers){
			
			if( isInCountry(earthquake, country) )							
				return true;	
		}
		
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes	
	private void printQuakes() 
	{
		
		int landQuakes = earthquakesPerCountry();
		int numberOfEarthquakes = quakeMarkers.size();
		
		for(int c = 0; c < countryMarkers.size(); c++){ 
			
			int numberOfQuakes = countryQuakes[c];
			
			if(numberOfQuakes > 0)
				System.out.println(countryMarkers.get(c).getProperty("name") + ": " + numberOfQuakes );
		}
		
		System.out.println("Total number of earthquakes: " + numberOfEarthquakes);
		System.out.println("Number of land earthquakes: " + landQuakes);
		System.out.println("Number of ocean earthquakes: " + (numberOfEarthquakes - landQuakes));		
		
	}
	

	// helper method which allocates the number of earthquakes per country in the countryQuakes array
	// and returns the number of land earthquakes
	// You will want to loop through the country markers or country features
		// (either will work) and then for each country, loop through
		// the quakes to count how many occurred in that country.
		// Recall that the country markers have a "name" property, 
		// And LandQuakeMarkers have a "country" property set.
	
	/**
	 * @return the number of land earthquakes
	 */
	private int earthquakesPerCountry(){
		
		int landQuakes = 0;
		countryQuakes = new int[countryMarkers.size()];
		
		for (int c = 0; c < countryMarkers.size(); c++){       
			
			String countryName = (String) countryMarkers.get(c).getProperty("name");
			
			for (Marker earthquake : quakeMarkers) {				
				
				if(earthquake instanceof LandQuakeMarker)
				{
					String countryName2 = (String)earthquake.getProperty("country");
																
					if(countryName.equals(countryName2)){
						countryQuakes[c]++;   
						landQuakes++;
					}
				}
			}		
		}
		
		return landQuakes;
	} 
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake 
	// feature if it's in one of the countries.
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {   // isInsideByLocation is an AbstractShapeMarker method
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}
