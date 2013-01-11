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

import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.xml.XmlAttribute;
import processing.core.PConstants;
import processing.core.PGraphics;

public class Eraser extends Brush {
	@XmlAttribute public int size;

	public Eraser(int size) {
		super();
		this.size = size;
		name = "Eraser " + size;
	}
	
	public Eraser(Eraser original) {
		super(original);
		size = original.size;
		name = original.name;
	}
	
	public Eraser(MoveableItem original) {
		super(original);
	}
	
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0) return;
		if (length == 1) {
			StrokePoint p = s.getPath().get(length-1);
			
			g.beginDraw();
			g.noStroke();
			g.fill(Application.getCanvas().backgroundColour);
			g.ellipseMode(PConstants.CENTER);
			g.ellipse(p.x, p.y, size, size);
			g.endDraw();
		} else {
			StrokePoint from = s.getPath().get(length-2);
			StrokePoint to = s.getPath().get(length-1);
			
			g.beginDraw();
			g.strokeJoin(PConstants.ROUND);
			g.strokeCap(PConstants.ROUND);
			g.stroke(Application.getCanvas().backgroundColour);
			g.strokeWeight(size);
			g.line(from.x, from.y, to.x, to.y);
			g.endDraw();
		}
	}

}
