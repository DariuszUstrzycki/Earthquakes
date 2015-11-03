package module5;

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
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
/**
 * @author a
 *
 */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setup and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
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
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
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
		
		assert ( howManySelectedQuakes() == 1 || howManySelectedQuakes() ==0 ) &&
		( howManySelectedCities() == 1 || howManySelectedCities() ==0 )
		: "selected quakes: " + howManySelectedQuakes() + "selected cities: " + howManySelectedCities();
		
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		
		// clear the last selection immediately after mouse leaves a marker's area
		if (lastSelected != null) {            // lastSelected is a private CommonMarker
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}
	
	// If there is a marker under the cursor, and lastSelected is null 
	// set the lastSelected to be the first marker found under the cursor
	// Make sure you do not select two markers.
	// 
	private void selectMarkerIfHover(List<Marker> markers)
	{
			
		for (Marker marker : markers) {
			
			if (marker.isInside(map, mouseX, mouseY)){
				
				if (lastSelected != null)
					lastSelected.setSelected(false); // remove the selection from the previously selected marker in the city/quake markers
				
				marker.setSelected(true);
				lastSelected = (CommonMarker)marker;
				break;				
			}
		}
	}
	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()
	{					
		System.out.println("CLICK!!!");
		
		boolean foundNewSelection = false;
		
		//look for a new selection only after unhiding took place >> lastClicked = null;
		if(lastClicked == null){
			foundNewSelection = checkMarkersForClick(cityMarkers);
		
			if (!foundNewSelection)
				foundNewSelection = checkMarkersForClick(quakeMarkers);
		}
		
		System.out.println("foundNewSelection: " + foundNewSelection);
		System.out.println("lastClicked: " + lastClicked);
		
		if(foundNewSelection){
			hideMarkers();			
		}
		else {
			
			if(lastClicked != null){
				unhideMarkers(); 
				// after unhiding remove clickSelection from the marker and from the lastClicked
				lastClicked.setClicked(false);
				lastClicked = null;
			}
		}
		
		
				
	}
	

	 
	private boolean checkMarkersForClick(List<Marker>  markers){
		
		for(Marker marker : markers ){
			
			if (marker.isInside(map, mouseX, mouseY)){
				
				System.out.println("This click is inside a marker.");
				
				if(clickOnPreviousSelection(marker)){
					return false;
				}
				else {
					((CommonMarker)marker).setClicked(true);
					lastClicked = (CommonMarker)marker;
					return true;
				}
			}
		}
		
		return false; // no new marker has been selected
	}
	
	private boolean clickOnPreviousSelection (Marker marker){
		
		boolean previousButtonClicked = false;
		
		if (lastClicked != null && marker == lastClicked){
			previousButtonClicked = true;
		}			
		else {
			previousButtonClicked =  false;	
		}
		
		System.out.println("The previous button has been clicked: " + previousButtonClicked);
		return previousButtonClicked;
				
	}
	
	
	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	
	private void hideMarkers(){
		
		for(Marker marker : quakeMarkers) {
			if( !(((CommonMarker)marker) == lastClicked )){  // don't hide the selected button	
				marker.setHidden(true);	
			}
			
			if (lastClicked instanceof CityMarker){
				leaveThreateningQuakesVisible(marker);
			}
			
		}
			
		for(Marker marker : cityMarkers) {
			if( !(((CommonMarker)marker) == lastClicked )){ // don't hide the selected marker	
					marker.setHidden(true);	
			}
			
			if (lastClicked instanceof EarthquakeMarker){
				leaveThreatenedCitiesVisible(marker);
				
				if (lastClicked instanceof OceanQuakeMarker){
					addCityToThreatenedCities(marker);
				}
			}
			
			
		}
	}
	
	private void leaveThreatenedCitiesVisible(Marker cityMarker){
		if ( (distanceToClickedMarker(cityMarker) <= threatCircleRadius((EarthquakeMarker)lastClicked) )){
			cityMarker.setHidden(false);
		}
	}
	
	private void leaveThreateningQuakesVisible(Marker quakeMarker){
		if ( (distanceToClickedMarker(quakeMarker) <= threatCircleRadius((EarthquakeMarker)quakeMarker) )){
			quakeMarker.setHidden(false);
		}
	}
	
	private void addCityToThreatenedCities(Marker cityMarker){
		
		if( (distanceToClickedMarker(cityMarker) <= threatCircleRadius((EarthquakeMarker)lastClicked) )){            
			
			((OceanQuakeMarker) lastClicked).setMarkersMap(map);
			((OceanQuakeMarker) lastClicked).addThreatenedCity((CityMarker) cityMarker);
		}
	}
	
	/*private void hideMarkers(){
		
		if(lastClicked instanceof EarthquakeMarker){
										
			
			for(Marker marker : quakeMarkers) {
				if( !((CommonMarker)marker).isClicked()){ 	
					marker.setHidden(true);	
				}
			}
				
			for(Marker marker : cityMarkers) {
				if( !(  ((CommonMarker)marker).isClicked() ||
				        (distanceToEpicenter(marker) <= threatCircleRadius((EarthquakeMarker)lastClicked) ))
					 ){                    
						marker.setHidden(true);	
				}
			}
		}	
			
		
		if(lastClicked instanceof CityMarker){
																
			for(Marker marker : cityMarkers) {
				
				if( !((CommonMarker)marker).isClicked() ){                    
					marker.setHidden(true);	
				}
			}	
			
			for(Marker marker : quakeMarkers) {
				
				if( ! (distanceToEpicenter( marker) <= threatCircleRadius((EarthquakeMarker)marker))){                                                                               //earthquakeWithinThreatCircle(marker, lastClickedLocation))
					marker.setHidden(true);	
				}
			}
		}
		
		if(lastClicked instanceof OceanQuakeMarker){
			
			System.out.println("/////////////////Entering lastClicked instanceof OceanQuakeMarker/////////////////");
			
			for(Marker marker : cityMarkers) {
				if( (distanceToEpicenter(marker) <= threatCircleRadius((EarthquakeMarker)lastClicked) )){
					                 
					//ScreenPosition position = map.getScreenPosition(marker.getLocation());
					//System.out.println("marker.getLocation():  " + marker.getLocation());
					//System.out.println("position.x " + position.x + " position.y: " + position.y);
						//((OceanQuakeMarker) lastClicked).addToThreatenedCities((CityMarker) marker);
					((OceanQuakeMarker) lastClicked).setMarkersMap(map);
					((OceanQuakeMarker) lastClicked).addThreatenedCity((CityMarker) marker);
				}
			}
				
			
			
			//System.out.println(lastClicked.getDistanceTo(Location loc));
			System.out.println("threatCircle: " + ((OceanQuakeMarker) lastClicked).threatCircle());
			System.out.println("getScreenPosition: " + lastClicked.getScreenPosition(map));
			System.out.println("getLocation: " + lastClicked.getLocation());
			System.out.println("lastClicked: " + lastClicked.getProperties());
			//System.out.println( lastClicked.drawMarker(pg, x, y););
			//System.out.println( ((OceanQuakeMarker) lastClicked).drawEarthquake(pg, x, y););
		}
	}*/
		
	private double threatCircleRadius(EarthquakeMarker marker){
		return marker.threatCircle();
	}	
	
	/**
	 * @param a marker
	 * @return the distance from the given marker to the last clicked marker
	 */
	private double distanceToClickedMarker(Marker marker){		
		return marker.getDistanceTo(lastClicked.getLocation()); 		
	}
	
	
	
	
	
	// helper method to draw key in GUI
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 50;
		
		rect(xbase, ybase, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);
		
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);

		text("Past hour", xbase+50, ybase+200);
		
		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);
			
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.	
	private boolean isLand(PointFeature earthquake) {
		
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
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
	
	//TODO: remove the following 3 methods
	
	private int howManySelectedQuakes(){
		
		int c = 0;
		
		for( Marker quake : quakeMarkers )
		{
			if(quake.isSelected())
				c++;			
		}
		//System.out.println("Selected quakes: " + c);	
		
		return c;
	}
	
	private int howManySelectedCities(){
		
		int c = 0;
		
		for( Marker city : cityMarkers )
		{
			if(city.isSelected())
				c++;			
		}
		//System.out.println("Selected cities: " + c);
		
		return c;
	}
	
	private int howManySelectedTotal(){
		return howManySelectedQuakes() + howManySelectedCities();		
	}
	private UnfoldingMap getMap(){
		return map;
	}
}
