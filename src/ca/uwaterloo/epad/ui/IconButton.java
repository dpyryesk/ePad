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

import processing.core.PShape;
import vialab.SMT.Touch;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;

/**
 * This class creates a simple button widget that has an icon instead of text
 * and may invoke a certain method when pressed. To set the method to invoke use
 * either {@link #setStaticPressMethod(String, Class)} (for static methods) or
 * {@link #setPressMethod(String, Object)} (for instance methods).
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class IconButton extends Zone {
	private static final Logger LOGGER = Logger.getLogger(IconButton.class);

	// Variables used to render the button
	protected int backgroundColour;
	protected int iconColour;

	// Shape of the icon
	protected PShape icon;
	// Button down flag
	protected boolean buttonDown = false;
	// Method to invoke when the button is pressed
	protected Method pressMethod;
	// Object that contains the method to invoke (for instance methods)
	protected Object pressMethodObject;

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
	 * @param iconName
	 * @param backgroundColour
	 * @param iconColour
	 */
	public IconButton(int x, int y, int width, int height, String iconName, int backgroundColour, int iconColour) {
		super(x, y, width, height);

		icon = applet.loadShape(Settings.dataFolder + "vector\\" + iconName + ".svg");
		if (icon == null)
			LOGGER.error("Failed to load shape: " + Settings.dataFolder + "vector\\" + iconName + ".svg");
		else
			icon.disableStyle();

		this.backgroundColour = backgroundColour;
		this.iconColour = iconColour;
	}

	// Draw button
	@Override
	protected void drawImpl() {
		noStroke();
		fill(backgroundColour);
		ellipseMode(CORNER);
		ellipse(0, 0, width, height);

		if (icon != null) {
			fill(iconColour);
			shapeMode(CENTER);
			shape(icon, width / 2, height / 2, width * 0.75f, height * 0.75f);
		}
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

	// Invoke the specified method
	protected void invokePress() {
		if (pressMethod != null) {
			try {
				pressMethod.invoke(pressMethodObject);
			} catch (Exception e) {
				LOGGER.error("Failed to invoke method " + pressMethod.toString() + ". " + e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Set a static method to be invoked when the button is pressed. The method
	 * must not have any arguments.
	 * 
	 * @param methodName
	 *            name of the method
	 * @param c
	 *            class that contains the static method
	 */
	public void setStaticPressMethod(String methodName, Class<?> c) {
		pressMethodObject = null;
		try {
			pressMethod = c.getDeclaredMethod(methodName);
		} catch (Exception e) {
			LOGGER.error("Failed to get static method. " + e.getLocalizedMessage());
		}
	}

	/**
	 * Set an instance method to be invoked when the button is pressed. The
	 * method must not have any arguments.
	 * 
	 * @param methodName
	 *            name of the method
	 * @param obj
	 *            object that contains the instance method
	 */
	public void setPressMethod(String methodName, Object obj) {
		Class<?> c = obj.getClass();
		pressMethodObject = obj;
		try {
			pressMethod = c.getDeclaredMethod(methodName);
		} catch (Exception e) {
			LOGGER.error("Failed to get method. " + e.getLocalizedMessage());
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