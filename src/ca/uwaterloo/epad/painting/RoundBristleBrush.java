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

/**
 * This class simulates a circular hard bristle brush using a cloud of particles
 * approach. It has a single parameter <b>diameter</b>.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see Brush
 * @see MoveableItem
 */
public class RoundBristleBrush extends Brush {
	/**
	 * The diameter of the brush.</br>This parameter can be retrieved
	 * automatically from XML files using
	 * {@link ca.uwaterloo.epad.xml.SimpleMarshaller SimpleMarshaller} class.
	 */
	@XmlAttribute
	public int diameter;

	// Array of Bristle objects that represent the particle cloud
	private ArrayList<Bristle> bristleList;
	private int numBristles = 100;
	private float bristleSizeMin = 2;
	private float bristleSizeMax = 3;

	/**
	 * Default constructor that allows creating RoundBristleBrush objects
	 * manually.
	 * 
	 * @param diameter
	 *            diameter of the brush.
	 * 
	 */
	public RoundBristleBrush(int diameter) {
		super();
		this.diameter = diameter;
	}

	/**
	 * Constructor that builds a copy of another RoundBristleBrush object
	 * 
	 * @param original
	 *            the original RoundBristleBrush object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public RoundBristleBrush(RoundBristleBrush original) {
		super(original);
		diameter = original.diameter;
	}

	/**
	 * Constructor that builds a copy of another MoveableItem object
	 * 
	 * @param original
	 *            the original MoveableItem object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public RoundBristleBrush(MoveableItem original) {
		super(original);
	}

	/**
	 * RoundBristleBrush widget renders a stroke by painting a cloud of
	 * {@link Bristle} objects.
	 * 
	 * @see Bristle#draw(PGraphics)
	 */
	@Override
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0)
			return;
		if (length == 1)
			makeBristles(); // make a new pattern of bristles

		StrokePoint p = s.getPath().get(length - 1);

		g.beginDraw();
		g.noStroke();
		g.fill(colour);
		g.translate(p.x, p.y);
		for (Bristle b : bristleList) {
			b.draw(g);
		}
		g.endDraw();
	}

	/**
	 * Generate a random cloud of {@link Bristle} objects such that they are all
	 * located within the dimensions of the brush.
	 */
	private void makeBristles() {
		// Calculate the radius of the brush
		final float r = (float) diameter / 2;
		// Calculate the number of bristles
		numBristles = (int) Math.round(Math.PI * r * r / 3);

		// Clear the list
		if (bristleList == null) {
			bristleList = new ArrayList<Bristle>();
		} else {
			bristleList.clear();
		}

		// Populate the list with a number of randomly generated Bristle objects
		float x, y, size;
		for (int i = 0; i < numBristles; i++) {
			x = applet.random(-r, r);
			y = applet.random(-r, r);
			// Discard objects that are not located within the circle
			if (Math.sqrt(x * x + y * y) > r) {
				i--;
			} else {
				size = applet.random(bristleSizeMin, bristleSizeMax);
				bristleList.add(new Bristle(x, y, size));
			}
		}
	}
}
