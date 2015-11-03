package com.ustrzycki.ufoldingmaps.week3module4;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/** Implements a visual marker for land earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Dariusz Ustrzycki
 *
 */
public class LandQuakeMarker extends EarthquakeMarker {
	
	
	public LandQuakeMarker(PointFeature quake) {
		
		// calling EarthquakeMarker constructor
		super(quake);
		
		// setting field in earthquake marker
		isOnLand = true;
	}


	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		
		// draw a centered circle for ocean quakes		
		pg.ellipse(x, y, getRadius(), getRadius());		
	}
	
	// draw an inner circle if earthquake happened within the past day
	@Override
	public  void drawPastDay(PGraphics pg, float x, float y){
		pg.fill(EarthquakeMarker.BLACK);
		pg.ellipse(x, y, getRadius()/2, getRadius()/2);	
	}	

	// Get the country the earthquake is in
	public String getCountry() {
		return (String) getProperty("country");
	}
}