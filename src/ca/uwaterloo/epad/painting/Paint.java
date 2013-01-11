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

import processing.core.PImage;
import vialab.SMT.Touch;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.xml.XmlAttribute;

public class Paint extends MoveableItem {
	@XmlAttribute public int paintColour;
	
	protected static PImage paintImage;

	public Paint(int colour) {
		super(0, 0, 150, 150);
		this.paintColour = colour;
		isSelected = false;
		name = ""+colour;
		
		if (paintImage == null)
			paintImage = applet.loadImage("data\\images\\paintCan.png");
	}
	
	public Paint(Paint original) {
		super(original);
		paintColour = original.paintColour;
		isSelected = false;
	}
	
	public Paint(MoveableItem original) {
		super(original);
		
		if (paintImage == null)
			paintImage = applet.loadImage("..\\data\\images\\paintCan.png");
	}
	
	protected void drawItem() {
		imageMode(CENTER);
		tint(paintColour);
		image(paintImage, width/2, height/2, 100, 100);
	}
	
	protected void doTouchDown(Touch touch) {
		Application.setPaint(this);
	}
	
	protected void doTouchUp(Touch touch) {
		Application.setPaint(null);
	}
	
	public int getColour() {
		return paintColour;
	}
}
