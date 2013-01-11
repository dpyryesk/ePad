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

package ca.uwaterloo.epad.painting;

import processing.core.PGraphics;
import vialab.SMT.Touch;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;

public abstract class Brush extends MoveableItem {
	public Brush() {
		super(0, 0, 150, 150);
		isSelected = false;
		name = ""+width+"x"+height;
	}
	
	public Brush(Brush original) {
		super(original);
		isSelected = false;
	}
	
	public Brush(MoveableItem original) {
		super(original);
		isSelected = false;
	}
	
	protected void doTouchDown(Touch touch) {
		Application.setBrush(this);
	}
	
	protected void doTouchUp(Touch touch) {
		Application.setBrush(null);
	}
	
	protected void drawItem() {
		if (itemImage == null) {
			fill(secondaryColour);
			noStroke();
			rectMode(CENTER);
			rect(width / 2, height / 2, 100, 100);
			fill(0);
			text(name, 30, 30);
		} else {
			imageMode(CENTER);
			image(itemImage, width / 2, height / 2, 125, 125);
		}
	}
	
	public abstract void renderStroke(Stroke s, int colour, PGraphics g);
}
