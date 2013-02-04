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

/**
 * This class represents a single point in a stroke object. It contains
 * information copied from a Touch object that may be used by a {@link Brush} to
 * render the stroke, such as x and y coordinates, motion speed, acceleration,
 * etc.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see Stroke
 */
public class StrokePoint {
	public float x, y, width, height, xSpeed, ySpeed, motionSpeed, motionAcceleration;

	/**
	 * Default constructor.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param xSpeed
	 * @param ySpeed
	 * @param motionSpeed
	 * @param motionAcceleration
	 */
	public StrokePoint(float x, float y, float width, float height, float xSpeed, float ySpeed, float motionSpeed, float motionAcceleration) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.xSpeed = xSpeed;
		this.ySpeed = ySpeed;
		this.motionSpeed = motionSpeed;
		this.motionAcceleration = motionAcceleration;
	}

	/**
	 * Find the distance between two points.
	 * 
	 * @param p
	 *            another point
	 * @return distance between the points
	 */
	public float dist(StrokePoint p) {
		float dx = x - p.x;
		float dy = y - p.y;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}
}
