/*
 *	ePad 2.0 Multitouch Customizable Painting Platform
 *  Copyright (C) 2012 Dmitry Pyryeskin and Jesse Hoey, University of Waterloo
 *  
 *  This file is part of ePad 2.0.
 *
 *  ePad 2.0 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ePad 2.0 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with ePad 2.0. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.uwaterloo.epad.ui;

import processing.core.PApplet;
import processing.core.PVector;
import vialab.SMT.Zone;

public class RotatingDrawer extends Drawer {
	private int diameter;
	private float angle;
	
	public static RotatingDrawer makeLeftDrawer(PApplet parent) {
		RotatingDrawer instance = new RotatingDrawer(-parent.height*2, -parent.height/2, parent.height*2, LEFT);
		
		instance.container = new RotatingContainer(instance.diameter, instance);
		instance.add(instance.container);
		
		return instance;
	}
	
	public static RotatingDrawer makeRightDrawer(PApplet parent) {
		RotatingDrawer instance = new RotatingDrawer(parent.width, -parent.height/2, parent.height*2, RIGHT);
		
		instance.container = new RotatingContainer(instance.diameter, instance);
		instance.add(instance.container);
		
		return instance;
	}
	
	private RotatingDrawer (int x, int y, int diameter, int position) {
		super(x, y, diameter, diameter, position);
		
		this.position = position;
		this.diameter = diameter;
		
		if (position == LEFT) {
			angle = -HALF_PI;
			dragX = true;
			dragY = false;
			dragXMin = x;
			dragXMax = x + width + width/4;
			dragYMin = Integer.MIN_VALUE;
			dragYMax = Integer.MAX_VALUE;
		} else if (position == RIGHT) {
			angle = HALF_PI;
			dragX = true;
			dragY = false;
			dragXMin = x + width - width/4;
			dragXMax = x + width*2;
			dragYMin = Integer.MIN_VALUE;
			dragYMax = Integer.MAX_VALUE;
		}
		
		rotateAbout(angle, CENTER);
	}
	
	protected void drawImpl() {
		pushMatrix();
		
		translate(width/2, height/2);
		
		noStroke();
		fill(0xFF0099CC);
		
		ellipseMode(CENTER);
		ellipse(0, 0, diameter, diameter);
		
		translate(0, diameter/2);
		noStroke();
		triangle(-100, -7, 0, 60, 100, -7);
		
		stroke(0xFF33B5E5);
		strokeWeight(3);
		line(-100, -7, 0, 60);
		line(0, 60, 100, -7);
		
		popMatrix();
	}
	
	protected void pickDrawImpl() {
		pushMatrix();
		translate(width/2, height/2);
		ellipseMode(CENTER);
		ellipse(0, 0, diameter, diameter);
		translate(0, diameter/2);
		triangle(-100, -5, 0, 60, 100, -5);
		popMatrix();
	}
	
	protected boolean calculateState() {
		if (getVisibleWidth() > 30) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isItemAbove(Zone item) {
		PVector drawerCetre = getCentre();
		PVector itemCentre = item.getCentre();
		float d = drawerCetre.dist(itemCentre) - diameter/2;
		return d < 0;
	}
	
	public PVector getHandleLocation() {
		PVector handle = new PVector(width/2, height/2 + diameter/2 + 30);
		return fromZoneVector(handle);
	}
	
	public float getVisibleWidth() {
		PVector p = fromZoneVector(new PVector(x, y));
		float w = 0;
		if (position == LEFT) {
			w = width/4 - (x - p.x);
		} else if (position == RIGHT) {
			w = (x - p.x) + width + width/4;
		}
		
		return w;
	}
}
