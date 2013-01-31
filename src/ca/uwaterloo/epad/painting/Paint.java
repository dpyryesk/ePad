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

import org.apache.log4j.Logger;

import processing.core.PImage;
import vialab.SMT.Touch;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.xml.XmlAttribute;

/**
 * This class represents a paint widget that allows users to select different
 * colours for drawing. It has a single parameter <b>paintColour</b>.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see MoveableItem
 */
public class Paint extends MoveableItem {
	private static final Logger LOGGER = Logger.getLogger(Paint.class);

	/**
	 * The colour of the paint widget.</br>This parameter can be retrieved
	 * automatically from XML files using
	 * {@link ca.uwaterloo.epad.xml.SimpleMarshaller SimpleMarshaller} class.
	 */
	@XmlAttribute
	public int paintColour;

	// Static image that is shared between all paint widgets.
	protected static PImage paintImage;

	/**
	 * Default constructor that allows creating Paint objects manually.
	 * 
	 * @param colour
	 *            colour of the paint.
	 */
	public Paint(int colour) {
		super(0, 0, 150, 150);
		this.paintColour = colour;
		isSelected = false;

		// Load the paint image if it was not loaded yet
		if (paintImage == null) {
			paintImage = applet.loadImage(Settings.dataFolder + "images\\paintCan.png");
			if (paintImage == null)
				LOGGER.error("Failed to load image: " + Settings.dataFolder + "images\\paintCan.png");
		}
	}

	/**
	 * Constructor that builds a copy of another Paint object
	 * 
	 * @param original
	 *            the original Paint object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public Paint(Paint original) {
		super(original);
		paintColour = original.paintColour;
		isSelected = false;
	}

	/**
	 * Constructor that builds a copy of another MoveableItem object
	 * 
	 * @param original
	 *            the original MoveableItem object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public Paint(MoveableItem original) {
		super(original);

		// Load the paint image if it was not loaded yet
		if (paintImage == null) {
			paintImage = applet.loadImage(Settings.dataFolder + "images\\paintCan.png");
			if (paintImage == null)
				LOGGER.error("Failed to load image: " + Settings.dataFolder + "images\\paintCan.png");
		}
	}

	protected void drawItem() {
		imageMode(CENTER);
		tint(paintColour);
		image(paintImage, width / 2, height / 2, 100, 100);
	}

	protected void doTouchDown(Touch touch) {
		Application.setSelectedPaint(this);
	}

	protected void doTouchUp(Touch touch) {
	}

	/**
	 * 
	 * @return the colour of the paint widget.
	 */
	public int getColour() {
		return paintColour;
	}
}
