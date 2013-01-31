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

/**
 * This class represents a single stroke on the canvas.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see StrokePoint
 */
public class Stroke {
	/**
	 * Unique identifier of the stroke object. Is equal to the ID of the Touch
	 * object that created the stroke.
	 */
	public long id;

	// A collection of points that define the stroke
	private Vector<StrokePoint> path;
	private Canvas canvas;
	private Paint paint;
	private Brush brush;

	/**
	 * Default constructor creates a Stroke object and stores the coordinated of
	 * the Touch object translated to the coordinate space of the Canvas.
	 * 
	 * @param t
	 *            Touch object.
	 * @param c
	 *            Canvas.
	 */
	public Stroke(Touch t, Canvas c) {
		id = t.sessionID;
		canvas = c;
		path = new Vector<StrokePoint>();

		PVector v = canvas.toZoneVector(new PVector(t.x, t.y));
		getPath().add(new StrokePoint(v.x, v.y, 0, 0, t.xSpeed, t.ySpeed, t.motionSpeed, t.motionAcceleration));

		// Save the currently selected paint
		paint = Application.getSelectedPaint();
		// Save the currently selected brush
		brush = Application.getSelectedBrush();
	}

	/**
	 * Update the stroke by adding the coordinates of the given Touch object
	 * translated to the coordinate space of the Canvas to it.
	 * 
	 * @param t
	 *            Touch object.
	 */
	public void update(Touch t) {
		PVector v = canvas.toZoneVector(new PVector(t.x, t.y));
		getPath().add(new StrokePoint(v.x, v.y, 0, 0, t.xSpeed, t.ySpeed, t.motionSpeed, t.motionAcceleration));
	}

	/**
	 * Render the stroke based on the Brush and Paint objects.
	 * 
	 * @param g
	 *            PGraphics to render the stroke into.
	 * @see Brush#renderStroke(Stroke, int, PGraphics)
	 * @see Paint#getColour()
	 */
	public void render(PGraphics g) {
		int colour = 0;
		if (paint != null)
			colour = paint.getColour();

		if (brush != null)
			brush.renderStroke(this, colour, g);
	}

	/**
	 * 
	 * @return the entire path of the stroke.
	 */
	public Vector<StrokePoint> getPath() {
		return path;
	}

	/**
	 * 
	 * @return the last point of the stroke.
	 */
	public StrokePoint getLastPoint() {
		if (getPath().size() == 0)
			return null;
		else
			return getPath().lastElement();
	}
}
