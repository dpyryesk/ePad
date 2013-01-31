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

package ca.uwaterloo.epad.ui;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;
import processing.core.PShape;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;

/**
 * This class represents a close button widget that can be added to any zone and
 * will attempt to close or remove the parent when pressed.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class CloseButton extends Zone {
	private static final Logger LOGGER = Logger.getLogger(CloseButton.class);

	// Variables used to render the button
	protected int backgroundColour = Application.deleteColour;
	protected int iconColour = Application.backgroundColour;

	// Shape of the icon
	protected static PShape icon;
	// Button down flag
	protected boolean buttonDown = false;

	/**
	 * Default constructor.
	 * 
	 * @param x
	 *            x-coordinate of the top left corner of the button
	 * @param y
	 *            y-coordinate of the top left corner of the button
	 * @param width
	 *            width of the button
	 * @param height
	 *            height of the button
	 */
	public CloseButton(int x, int y, int width, int height) {
		super(x, y, width, height);

		if (icon == null) {
			icon = applet.loadShape(Settings.dataFolder + "vector\\x.svg");
			if (icon == null)
				LOGGER.error("Failed to load shape: " + Settings.dataFolder + "vector\\x.svg");
			else
				icon.disableStyle();
		}
	}

	// Draw button
	@Override
	protected void drawImpl() {
		noStroke();
		fill(backgroundColour);
		ellipseMode(CORNER);
		ellipse(0, 0, width, height);

		fill(iconColour);
		shapeMode(CENTER);
		shape(icon, width / 2, height / 2, width * 0.75f, height * 0.75f);
	}

	// Draw for zone picker
	@Override
	protected void pickDrawImpl() {
		ellipseMode(CORNER);
		ellipse(0, 0, width, height);
	}

	// Action on touch event
	@Override
	protected void touchImpl() {
		Application.setActionPerformed();
	}

	// Action on touch up event
	@Override
	protected void touchUp(Touch touch) {
		buttonDown = getTouches().length > 0;
		super.touchUp(touch);

		if (buttonDown) {
			invokePress();
		}
		buttonDown = false;
	}

	// Action on touch down event
	@Override
	protected void touchDown(Touch touch) {
		super.touchDown(touch);
		buttonDown = true;
	}

	// Attempt to close or remove the parent zone
	protected void invokePress() {
		try {
			// Attempt to use the parent's close() method
			Class<?> c = parent.getClass();
			Method closeMethod = c.getDeclaredMethod("close");
			closeMethod.invoke(parent);
		} catch (Exception e) {
			if (parent != null) {
				// If it doesn't exist, simply remove parent from the client
				TouchClient.remove(parent);
			} else {
				// If the close button is on the top layer, use it to exit the
				// application
				applet.exit();
			}
		}
	}
	
	/**
	 * Set the colour scheme of the button.
	 * 
	 * @param backgroundColour
	 *            colour of the button's background
	 * @param iconColour
	 *            colour of the button's icon
	 */
	public void setColourScheme(int backgroundColour, int iconColour) {
		this.backgroundColour = backgroundColour;
		this.iconColour = iconColour;
	}
}