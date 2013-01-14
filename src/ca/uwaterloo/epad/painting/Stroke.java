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

import java.util.Vector;

import processing.core.PGraphics;
import processing.core.PVector;
import vialab.SMT.Touch;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.Canvas;


public class Stroke {
	public long id;
	private Vector<StrokePoint> path;
	private Canvas canvas;
	private Paint paint;
	private Brush brush;
	
	public Stroke(Touch t, Canvas c) {
		id = t.sessionID;
		canvas = c;
		path = new Vector<StrokePoint>();
		
		PVector v = canvas.toZoneVector(new PVector(t.x, t.y));
		getPath().add(new StrokePoint(v.x, v.y, 0, 0, t.xSpeed, t.ySpeed, t.motionSpeed, t.motionAcceleration));
		
		paint = Application.getSelectedPaint();
		brush = Application.getSelectedBrush();
	}
	
	public void update(Touch t) {
		PVector v = canvas.toZoneVector(new PVector(t.x, t.y));
		getPath().add(new StrokePoint(v.x, v.y, 0, 0, t.xSpeed, t.ySpeed, t.motionSpeed, t.motionAcceleration));
	}
	
	public StrokePoint getLastPoint() {
		if (getPath().size() == 0)
			return null;
		else return getPath().lastElement();
	}
	
	public void render(PGraphics g) {
		int colour = 0;
		if (paint != null)
			colour = paint.getColour();
		
		if (brush != null)
			brush.renderStroke(this, colour, g);
	}

	public Vector<StrokePoint> getPath() {
		return path;
	}
}
