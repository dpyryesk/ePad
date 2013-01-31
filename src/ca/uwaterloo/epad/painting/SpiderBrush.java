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

import java.util.ArrayList;
import java.util.Iterator;

import ca.uwaterloo.epad.ui.MoveableItem;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * This class represents a Spider Brush widget that produces unique effects when
 * painting. It has no parameters.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see Brush
 * @see MoveableItem
 * 
 */
public class SpiderBrush extends Brush {
	// List of previously entered points, shared between all Spider Brushes
	private static ArrayList<StrokePoint> pointList = new ArrayList<StrokePoint>();

	// Painting parameters
	private float minDistance = 10; // minimum distance between two points
	private float connectionRadius = 150; // maximum distance between point
											// connected by secondary lines
	private float lineWeight = 3; // weight of the primary lines
	private float lineWeight2 = 1; // weight of the secondary lines
	private float lineAlpha = 128; // transparency of the primary lines
	private float lineAlpha2 = 64; // transparency of the secondary lines

	/**
	 * Default constructor that allows creating SpiderBrush objects manually.
	 * 
	 */
	public SpiderBrush() {
		super();
	}

	/**
	 * Constructor that builds a copy of another SpiderBrush object
	 * 
	 * @param original
	 *            the original SpiderBrush object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public SpiderBrush(SpiderBrush original) {
		super(original);
	}

	/**
	 * Constructor that builds a copy of another MoveableItem object
	 * 
	 * @param original
	 *            the original MoveableItem object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public SpiderBrush(MoveableItem original) {
		super(original);
	}

	/**
	 * Spider Brush renders strokes by connecting them with primary lines
	 * (thicker) and then the points which are located close enough to each
	 * other are connected by secondary lines (thinner).
	 */
	@Override
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0)
			return;
		if (length == 1) {
			// Draw a circle in the beginning of the stroke
			StrokePoint p = s.getPath().get(length - 1);
			pointList.add(p);

			g.beginDraw();
			g.noStroke();
			g.fill(colour, lineAlpha);
			g.ellipseMode(PConstants.CENTER);
			g.ellipse(p.x, p.y, lineWeight, lineWeight);
			g.endDraw();
		} else {
			// Connect the new point to the previous by drawing a line
			StrokePoint from = pointList.get(pointList.size() - 1);
			StrokePoint to = s.getPath().get(length - 1);

			if (from.dist(to) > minDistance) {
				pointList.add(to);

				g.beginDraw();
				g.strokeWeight(lineWeight);
				g.stroke(colour, lineAlpha);
				g.strokeCap(ROUND);
				g.noFill();
				// g.tint(255, imageAlpha);
				g.line(from.x, from.y, to.x, to.y);

				g.strokeWeight(lineWeight2);
				g.stroke(colour, lineAlpha2);
				Iterator<StrokePoint> it = pointList.iterator();
				while (it.hasNext()) {
					StrokePoint p = it.next();
					if (PApplet.dist(p.x, p.y, to.x, to.y) <= connectionRadius)
						g.line(p.x, p.y, to.x, to.y);
				}

				g.endDraw();
			} else {
				from = s.getPath().get(length - 2);
				g.beginDraw();
				g.strokeWeight(lineWeight);
				g.stroke(colour, lineAlpha);
				g.strokeCap(ROUND);
				g.noFill();
				// g.tint(255, imageAlpha);
				g.line(from.x, from.y, to.x, to.y);
				g.endDraw();
			}
		}
	}

	/**
	 * Clear the list of previously entered points.
	 */
	public static void clearStrokes() {
		pointList.clear();
	}
}
