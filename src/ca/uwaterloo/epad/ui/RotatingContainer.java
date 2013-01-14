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

import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;
import vialab.SMT.Zone;


public class RotatingContainer extends Container  {
	// Layout parameters
	public static final int OFFSET_ANGLE = Settings.rotatingContainerOffsetAngle;	//angular offset between items in degrees
	public static final int OFFSET_DIST = Settings.rotatingContainerOffsetDistance;	//distance between rows of items
	public static final int ITEM_COUNT_MAX = 2 * 360 / OFFSET_ANGLE;	//maximum number of items container may hold
	
	private int diameter;
	
	public RotatingContainer (int diameter, Drawer parent) {
		super(0, 0, diameter, diameter, parent);
		this.diameter = diameter - 50;
	}
	
	public boolean addItem(Zone item) {
		if (itemCount >= ITEM_COUNT_MAX)
			return false;
		
		item.width = ITEM_WIDTH;
		item.height = ITEM_HEIGHT;
		
		float angle = itemCount * OFFSET_ANGLE * DEG_TO_RAD;
		item.rotateAbout(angle, width/2, height/2);
		if (itemCount < 360 / OFFSET_ANGLE)
			item.translate(width/2 - ITEM_WIDTH/2, height/2 + diameter/2 - (ITEM_HEIGHT + OFFSET_DIST));
		else
			item.translate(width/2 - ITEM_WIDTH/2, height/2 + diameter/2 - 2 * (ITEM_HEIGHT + OFFSET_DIST));
		
		add(item);
		
		items.put(new Integer(itemCount), item);
		
		itemCount++;
		
		return true;
	}
	
	protected void drawImpl() {
		pushMatrix();
		translate(width/2, height/2);
		
		noStroke();
		
		ellipseMode(CENTER);
		fill(backgroundColour);
		ellipse(0, 0, diameter, diameter);
		fill(primaryColour);
		ellipse(0, 0, diameter/2+100, diameter/2+100);
		
		popMatrix();
	}
	
	protected void pickDrawImpl() {
		ellipseMode(CENTER);
		ellipse(width/2, height/2, diameter, diameter);
	}
	
	protected void touchImpl() {
		rotateAboutCentre();
		Application.setActionPerformed();
		parent.setActionPerformed();
		notifyListeners(MOVED);
	}
}