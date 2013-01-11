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
	private float bristleSizeMax = 5;
	
	public RoundBristleBrush(int diameter) {
		super();
		this.diameter = diameter;
		name = "BristleBrush " + diameter;
	}
	
	public RoundBristleBrush(RoundBristleBrush original) {
		super(original);
		diameter = original.diameter;
		name = original.name;
	}
	
	public RoundBristleBrush(MoveableItem original) {
		super(original);
	}
	
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0) return;
		if (length == 1) makeBristles(); // make new pattern of bristles
		
		// wash colour out as stroke gets longer
		/*
		int alpha = 255 - length*3;
		if (alpha <= 0) alpha = 1;
		
		int red = (colour >> 16) & 0xFF;
		int green = (colour >> 8) & 0xFF;
		int blue = colour & 0xFF;
		*/
		
		StrokePoint p = s.getPath().get(length-1);
		
		g.beginDraw();
		g.noStroke();
		//g.fill(red, green, blue, alpha);
		g.fill(colour);
		g.translate(p.x, p.y);
		for (Bristle b : bristleList) {
			b.draw(g);
		}
		g.endDraw();
	}
	
	private void makeBristles() {
		numBristles = diameter * 2;
		
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
	
	private class Bristle {
		private float x, y, width;
		
		public Bristle(float x, float y, float width) {
			this.x = x;
			this.y = y;
			this.width = width;
		}
		
		public void draw(PGraphics g) {
			g.ellipse(x, y, width, width);
		}
	}

}
