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

/**
 * 
 * This class represents a single bristle of a hard bristle brush.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 */
class Bristle {
	private float x, y, diameter;

	/**
	 * Default constructor.
	 * 
	 * @param x
	 *            x coordinate of the bristle
	 * @param y
	 *            y coordinate of the bristle
	 * @param diameter
	 *            diameter of the bristle
	 */
	public Bristle(float x, float y, float diameter) {
		this.x = x;
		this.y = y;
		this.diameter = diameter;
	}

	/**
	 * Draw the bristle as a circle with the specified x and y coordinates and
	 * diameter. This method does not specify colour of the bristle. It must
	 * also be surrounded by PGraphics.beginDraw() and PGraphics.endDraw().
	 * 
	 * @param g
	 *            the graphics object to draw the bristle into
	 */
	public void draw(PGraphics g) {
		g.ellipse(x, y, diameter, diameter);
	}
}