package ca.uwaterloo.epad.ui;

import processing.core.PApplet;
import processing.core.PVector;
import vialab.SMT.Zone;

public class SlidingDrawer extends Drawer {
	public static SlidingDrawer makeTopDrawer(PApplet parent) {
		SlidingDrawer instance = new SlidingDrawer(0, -parent.height/3, parent.width, parent.height/3, TOP);
		
		instance.container = new StaticContainer(instance.width, instance.height, instance);
		instance.add(instance.container);
		
		return instance;
	}
	
	private SlidingDrawer (int x, int y, int width, int height, int position) {
		super(x, y, width, height, position);
		
		if (position == TOP) {
			dragX = false;
			dragY = true;
			dragXMin = Integer.MIN_VALUE;
			dragXMax = Integer.MAX_VALUE;
			dragYMin = y;
			dragYMax = y + height*2;
		}
	}
	
	protected void drawImpl() {
		pushMatrix();
		
		if (position == TOP) {
			noStroke();
			fill(0xFF0099CC);
			
			rectMode(CORNER);
			rect(0, 0, width, height);
			
			translate(width/2, height);
			noStroke();
			triangle(-100, 0, 0, 60, 100, 0);
			
			stroke(0xFF33B5E5);
			strokeWeight(3);
			line(-100, 0, 0, 60);
			line(0, 60, 100, 0);
		}
		
		popMatrix();
	}
	
	protected void pickDrawImpl() {
		pushMatrix();
		
		if (position == TOP) {
			rectMode(CORNER);
			rect(0, 0, width, height);
			translate(width/2, height);
			triangle(-100, 0, 0, 60, 100, 0);
		}
		
		popMatrix();
	}
	
	public boolean isOpen() {
		if (getVisibleWidth() > 30) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isItemAbove(Zone item) {
		PVector drawerCentre = getCentre();
		PVector itemCentre = item.getCentre();
		float d = 0;
		
		if (position == TOP) {
			d = itemCentre.y - drawerCentre.y - height/2;
		}
		
		return d < 0;
	}
	
	public PVector getHandleLocation() {
		if (position == TOP) {
			PVector handle = new PVector(width/2, height + 30);
			return fromZoneVector(handle);
		} else return null;
	}
	
	public float getVisibleWidth() {
		PVector p = fromZoneVector(new PVector(x, y));
		float w = 0;
		if (position == TOP) {
			w = height - y + p.y;
		}
		
		return w;
	}
}
