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
 * This class simulates a rectangular hard bristle brush using a cloud of
 * particles approach. It has two parameters: <b>brushWidth</b> and
 * <b>brushHeight</b>.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see Brush
 * @see MoveableItem
 */
public class RectangularBristleBrush extends Brush {
	/**
	 * The width of the brush.</br>This parameter can be retrieved automatically
	 * from XML files using {@link ca.uwaterloo.epad.xml.SimpleMarshaller
	 * SimpleMarshaller} class.
	 */
	@XmlAttribute
	public int brushWidth;

	/**
	 * The height of the brush.</br>This parameter can be retrieved
	 * automatically from XML files using
	 * {@link ca.uwaterloo.epad.xml.SimpleMarshaller SimpleMarshaller} class.
	 */
	@XmlAttribute
	public int brushHeight;

	// Array of Bristle objects that represent the particle cloud
	private ArrayList<Bristle> bristleList;
	private int numBristles = 100;
	private float bristleSizeMin = 2;
	private float bristleSizeMax = 3;

	/**
	 * Default constructor that allows creating RectangularBristleBrush objects
	 * manually.
	 * 
	 * @param width
	 *            width of the brush.
	 * @param height
	 *            height of the brush.
	 * 
	 */
	public RectangularBristleBrush(int width, int height) {
		brushWidth = width;
		brushHeight = height;
	}

	/**
	 * Constructor that builds a copy of another RectangularBristleBrush object
	 * 
	 * @param original
	 *            the original RectangularBristleBrush object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public RectangularBristleBrush(RectangularBristleBrush original) {
		super(original);
		brushWidth = original.brushWidth;
		brushHeight = original.brushHeight;
	}

	/**
	 * Constructor that builds a copy of another MoveableItem object
	 * 
	 * @param original
	 *            the original MoveableItem object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public RectangularBristleBrush(MoveableItem original) {
		super(original);
	}

	/**
	 * RectangularBristleBrush widget renders a stroke by painting a cloud of
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
		// Calculate the number of bristles
		numBristles = Math.round((float) (brushWidth * brushHeight) / 3);

		// Clear the list
		if (bristleList == null) {
			bristleList = new ArrayList<Bristle>();
		} else {
			bristleList.clear();
		}

		// Populate the list with a number of randomly generated Bristle objects
		float x, y, size;
		for (int i = 0; i < numBristles; i++) {
			x = applet.random(-brushWidth / 2, brushWidth / 2);
			y = applet.random(-brushHeight / 2, brushHeight / 2);
			size = applet.random(bristleSizeMin, bristleSizeMax);
			bristleList.add(new Bristle(x, y, size));
		}
	}
}
