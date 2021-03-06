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
 * This class represents a square sliding drawer widget. The child items are
 * organised by a {@link RectangularContainer}. Currently only TOP position is
 * supported.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see RectangularContainer
 */
public class SlidingDrawer extends Drawer {
	/**
	 * Automatically make a sliding drawer on the top of the screen. Width of
	 * the drawer is equal to the width of the screen and depth of the drawer is
	 * equal to the specified value.
	 * 
	 * @param parent
	 *            parent applet
	 * @param drawerDepth
	 *            depth of the drawer in pixels
	 * @return resulting SlidingDrawer instance
	 */
	public static SlidingDrawer makeTopDrawer(PApplet parent, int drawerDepth) {
		SlidingDrawer instance = new SlidingDrawer(0, -drawerDepth, parent.width, drawerDepth, TOP);

		instance.container = new RectangularContainer(instance.width, instance.height, instance);
		instance.add(instance.container);

		return instance;
	}

	// Constructor is private to prevent manual instantiation
	private SlidingDrawer(int x, int y, int width, int height, int position) {
		super(x, y, width, height, position);

		if (position == TOP) {
			dragX = false;
			dragY = true;
			dragXMin = Integer.MIN_VALUE;
			dragXMax = Integer.MAX_VALUE;
			dragYMin = y;
			dragYMax = y + height * 2;
		}
	}

	// Draw the drawer
	@Override
	protected void drawImpl() {
		if (autoClose) {
			float v = getVisibleWidth();
			if (v < openWidth && v > 0 && !isTouched) {
				if (position == TOP)
					slide(0, -Settings.drawerAutoClosingSpeed);
			}

			// TODO: close automatically after a certain period of time
//			if (System.currentTimeMillis() - lastUpdate.getMicroseconds() > 1000) {
//				slide(-5, -5);
//			}
		}

		pushMatrix();

		if (position == TOP) {
			noStroke();
			fill(secondaryColour);

			rectMode(CORNER);
			rect(0, 0, width, height);

			translate(width / 2, height);
			noStroke();
			triangle(-100, 0, 0, 60, 100, 0);

			stroke(primaryColour);
			strokeWeight(3);
			line(-100, 0, 0, 60);
			line(0, 60, 100, 0);
		}

		popMatrix();
	}

	// Draw for zone picker
	@Override
	protected void pickDrawImpl() {
		pushMatrix();

		if (position == TOP) {
			rectMode(CORNER);
			rect(0, 0, width, height);
			translate(width / 2, height);
			triangle(-100, 0, 0, 60, 100, 0);
		}

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
		PVector drawerCentre = getCentre();
		PVector itemCentre = item.getCentre();
		float d = 0;

		if (position == TOP) {
			d = itemCentre.y - drawerCentre.y - height / 2;
		}

		return d < 0;
	}

	@Override
	public PVector getHandleLocation() {
		if (position == TOP) {
			PVector handle = new PVector(width / 2, height + 30);
			return fromZoneVector(handle);
		} else
			return null;
	}

	@Override
	public float getVisibleWidth() {
		PVector p = fromZoneVector(new PVector(x, y));
		float w = 0;
		if (position == TOP) {
			w = height - y + p.y;
		}

		return w;
	}
}
