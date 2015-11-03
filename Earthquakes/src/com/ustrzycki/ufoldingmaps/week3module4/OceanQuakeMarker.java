 package com.ustrzycki.ufoldingmaps.week3module4;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/** Implements a visual marker for ocean earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Dariusz Ustrzycki
 *
 */
public class OceanQuakeMarker extends EarthquakeMarker {
	
	
	
	public OceanQuakeMarker(PointFeature quake) {
		super(quake);
		
		// setting field in earthquake marker
		isOnLand = false;
	}
	

	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		
		// Drawing a centered square for Ocean earthquakes
		pg.rect(x - ( getRadius()/2 ), y - ( getRadius()/2 ), getRadius(), getRadius());
	}
	
	// draw an inner square if earthquake happened within the past day
	@Override
	public  void drawPastDay(PGraphics pg, float x, float y){
		pg.fill(EarthquakeMarker.BLACK);
		pg.rect(x - ( getRadius()/4 ), y - ( getRadius()/4 ), getRadius()/2, getRadius()/2);
	}
	

}
