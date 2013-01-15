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

public class SpiderBrush extends Brush {
	private static ArrayList<StrokePoint> pointList = new ArrayList<StrokePoint>();
	private float minDistance = 10;
	private float connectionRadius = 150;
	private float lineWeight = 3;
	private float lineWeight2 = 1;
	private float lineAlpha = 128;
	private float lineAlpha2 = 64;

	public SpiderBrush() {
		super();
		name = "SpiderBrush";
	}

	public SpiderBrush(SpiderBrush original) {
		super(original);
		name = "SpiderBrush";
	}
	
	public SpiderBrush(MoveableItem original) {
		super(original);
	}

	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0)
			return;
		if (length == 1) {
			StrokePoint p = s.getPath().get(length - 1);
			pointList.add(p);

			g.beginDraw();
			g.noStroke();
			g.fill(colour, lineAlpha);
			g.ellipseMode(PConstants.CENTER);
			g.ellipse(p.x, p.y, lineWeight, lineWeight);
			g.endDraw();
		} else {
			StrokePoint from = pointList.get(pointList.size()-1);
			StrokePoint to = s.getPath().get(length - 1);
			
			if (from.dist(to) > minDistance) {
				pointList.add(to);
				
				g.beginDraw();
				g.strokeWeight(lineWeight);
				g.stroke(colour, lineAlpha);
				g.strokeCap(ROUND);
				g.noFill();
				//g.tint(255, imageAlpha);
				g.line(from.x, from.y, to.x, to.y);
				
				g.strokeWeight(lineWeight2);
				g.stroke(colour, lineAlpha2);
				Iterator<StrokePoint> it = pointList.iterator();
				while(it.hasNext()) {
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
				//g.tint(255, imageAlpha);
				g.line(from.x, from.y, to.x, to.y);
				g.endDraw();
			}
		}
	}
	
	public static void clearStrokes() {
		pointList.clear();
	}
}
