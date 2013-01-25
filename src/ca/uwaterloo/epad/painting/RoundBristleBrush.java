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

import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.xml.XmlAttribute;

import processing.core.PGraphics;

public class RoundBristleBrush extends Brush {
	@XmlAttribute public int diameter;
	
	private ArrayList<Bristle> bristleList;
	private int numBristles = 100;
	private float bristleSizeMin = 2;
	private float bristleSizeMax = 3;
	
	public RoundBristleBrush(int diameter) {
		super();
		this.diameter = diameter;
	}
	
	public RoundBristleBrush(RoundBristleBrush original) {
		super(original);
		diameter = original.diameter;
	}
	
	public RoundBristleBrush(MoveableItem original) {
		super(original);
	}
	
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0) return;
		if (length == 1) makeBristles(); // make a new pattern of bristles
		
		StrokePoint p = s.getPath().get(length-1);
		
		g.beginDraw();
		g.noStroke();
		g.fill(colour);
		g.translate(p.x, p.y);
		for (Bristle b : bristleList) {
			b.draw(g);
		}
		g.endDraw();
	}
	
	private void makeBristles() {
		final float r = (float) diameter / 2;
		numBristles = (int) Math.round(Math.PI * r * r / 10);
		
		if (bristleList == null) {
			bristleList = new ArrayList<Bristle>();
		} else {
			bristleList.clear();
		}
		
		float x, y, size;
		for (int i=0; i<numBristles; i++) {
			x = applet.random(-diameter/2, diameter/2);
			y = applet.random(-diameter/2, diameter/2);
			if (Math.sqrt(x*x + y*y) > diameter/2) {
				i--;
			} else {
				size = applet.random(bristleSizeMin, bristleSizeMax);
				bristleList.add(new Bristle(x, y, size));
			}
		}
	}
}
