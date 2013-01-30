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
import vialab.SMT.Touch;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;

/**
 * 
 * This abstract class implements the common functionality of all painting tools
 * in ePad 2.0 application. Every new painting tool must extend this class and
 * implement its abstract method {@link #renderStroke(Stroke, int, PGraphics)
 * renderStroke}.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 */
public abstract class Brush extends MoveableItem {
	/**
	 * Default constructor builds a Brush object with dimension 150x150 px.
	 */
	public Brush() {
		super(0, 0, 150, 150);
		isSelected = false;
	}

	/**
	 * Constructor that builds a copy of another Brush object
	 * 
	 * @param original
	 *            the original Brush object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public Brush(Brush original) {
		super(original);
		isSelected = false;
	}

	/**
	 * Constructor that builds a copy of another MoveableItem object
	 * 
	 * @param original
	 *            the original MoveableItem object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public Brush(MoveableItem original) {
		super(original);
		isSelected = false;
	}

	protected void doTouchDown(Touch touch) {
		Application.setSelectedBrush(this);
	}

	protected void doTouchUp(Touch touch) {

	}

	protected void drawItem() {
		if (itemImage == null) {
			fill(secondaryColour);
			noStroke();
			rectMode(CENTER);
			rect(width / 2, height / 2, 100, 100);
			fill(0);
			text(name, 30, 30);
		} else {
			imageMode(CENTER);
			image(itemImage, width / 2, height / 2, 125, 125);
		}
	}

	/**
	 * 
	 * Abstract method that defines how each brush stroke should be rendered
	 * 
	 * @param s
	 *            {@link Stroke} object
	 * @param colour
	 *            colour of the stroke
	 * @param g
	 *            PGraphics object to render the stroke into
	 */
	public abstract void renderStroke(Stroke s, int colour, PGraphics g);
}
