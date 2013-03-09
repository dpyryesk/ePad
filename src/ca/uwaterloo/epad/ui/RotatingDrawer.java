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

import ca.uwaterloo.epad.util.Settings;
import processing.core.PApplet;
import processing.core.PVector;
import vialab.SMT.Zone;

/**
 * This class represents a circular rotating drawer widget. The child items are
 * organised into circular pattern using a {@link RotatingContainer}. Currently
 * only LEFT and RIGHT positions are supported.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see RotatingContainer
 */
public class RotatingDrawer extends Drawer {
	// Diameter of the drawer
	private int diameter;
	// Angle of rotation (depends on position)
	private float angle;

	/**
	 * Automatically make a rotating drawer on the left side of the screen.
	 * Diameter of the drawer is equal to the height of the screen.
	 * 
	 * @param parent
	 *            parent applet
	 * @return resulting RotatingDrawer instance
	 */
	public static RotatingDrawer makeLeftDrawer(PApplet parent) {
		RotatingDrawer instance = new RotatingDrawer(-parent.height * 2, -parent.height / 2, parent.height * 2, LEFT);

		instance.container = new RotatingContainer(instance.diameter, instance);
		instance.add(instance.container);

		return instance;
	}

	/**
	 * Automatically make a rotating drawer on the right side of the screen.
	 * Diameter of the drawer is equal to the height of the screen.
	 * 
	 * @param parent
	 *            parent applet
	 * @return resulting RotatingDrawer instance
	 */
	public static RotatingDrawer makeRightDrawer(PApplet parent) {
		RotatingDrawer instance = new RotatingDrawer(parent.width, -parent.height / 2, parent.height * 2, RIGHT);

		instance.container = new RotatingContainer(instance.diameter, instance);
		instance.add(instance.container);

		return instance;
	}

	// Constructor is private to prevent manual instantiation
	private RotatingDrawer(int x, int y, int diameter, int position) {
		super(x, y, diameter, diameter, position);

		this.position = position;
		this.diameter = diameter;

		if (position == LEFT) {
			angle = -HALF_PI;
			dragX = true;
			dragY = false;
			dragXMin = x;
			dragXMax = x + width + width / 4;
			dragYMin = Integer.MIN_VALUE;
			dragYMax = Integer.MAX_VALUE;
		} else if (position == RIGHT) {
			angle = HALF_PI;
			dragX = true;
			dragY = false;
			dragXMin = x + width - width / 4;
			dragXMax = x + width * 2;
			dragYMin = Integer.MIN_VALUE;
			dragYMax = Integer.MAX_VALUE;
		}

		rotateAbout(angle, CENTER);
	}

	// Draw the drawer
	@Override
	protected void drawImpl() {
		if (autoClose) {
			float v = getVisibleWidth();
			if (v < openWidth && v > 0 && !isTouched) {
				if (position == LEFT)
					slide(-Settings.drawerAutoClosingSpeed, 0);
				else if (position == RIGHT)
					slide(Settings.drawerAutoClosingSpeed, 0);
			}

			// TODO: close automatically after a certain period of time
//			if (System.currentTimeMillis() - lastUpdate.getMicroseconds() > 1000) {
//				slide(-5, -5);
//			}
		}
		
		pushMatrix();

		translate(width / 2, height / 2);

		noStroke();
		fill(secondaryColour);

		ellipseMode(CENTER);
		ellipse(0, 0, diameter, diameter);

		translate(0, diameter / 2);
		noStroke();
		triangle(-100, -7, 0, 60, 100, -7);

		stroke(primaryColour);
		strokeWeight(3);
		line(-100, -7, 0, 60);
		line(0, 60, 100, -7);

		popMatrix();
	}

	// Draw for zone picker
	@Override
	protected void pickDrawImpl() {
		pushMatrix();
		translate(width / 2, height / 2);
		ellipseMode(CENTER);
		ellipse(0, 0, diameter, diameter);
		translate(0, diameter / 2);
		triangle(-100, -5, 0, 60, 100, -5);
		popMatrix();
	}

	// Calculate whether or not the drawer is opened
	@Override
	protected boolean calculateOpenedState() {
		if (getVisibleWidth() >= openWidth) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isItemAbove(Zone item) {
		PVector drawerCetre = getCentre();
		PVector itemCentre = item.getCentre();
		float d = drawerCetre.dist(itemCentre) - diameter / 2;
		return d < 0;
	}

	@Override
	public PVector getHandleLocation() {
		PVector handle = new PVector(width / 2, height / 2 + diameter / 2 + 30);
		return fromZoneVector(handle);
	}

	@Override
	public float getVisibleWidth() {
		PVector p = fromZoneVector(new PVector(x, y));
		float w = 0;
		if (position == LEFT) {
			w = width / 4 - (x - p.x);
		} else if (position == RIGHT) {
			w = (x - p.x) + width + width / 4;
		}

		return w;
	}
}
