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

import processing.core.PFont;
import vialab.SMT.Touch;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;

/**
 * This class creates a simple button widget that may invoke a certain method
 * when pressed. To set the method to invoke use either
 * {@link #setStaticPressMethod(String, Class)} (for static methods) or
 * {@link #setPressMethod(String, Object)} (for instance methods).
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class Button extends Zone {
	private static final Logger LOGGER = Logger.getLogger(Button.class);

	// Variables used to render the button
	protected int fontSize;
	protected String text;
	protected PFont font;
	protected float cornerRadius = 12;
	protected int colour = Application.primaryColour;
	protected int pressedColour = Application.secondaryColour;
	protected int borderWeight = 2;
	protected int borderColour = Application.secondaryColour;
	protected int textColour = 0;
	protected int pressedTextColour = 0;

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
	 *            height of the button
	 * @param text
	 *            text to display on the button
	 * @param fontSize
	 *            size of the font
	 * @param font
	 *            PFont to use
	 */
	public Button(int x, int y, int width, int height, String text, int fontSize, PFont font) {
		super(x, y, width, height);
		this.text = text;
		this.fontSize = fontSize;
		this.font = font;
	}

	// Draw button
	@Override
	protected void drawImpl() {
		if (buttonDown) {
			drawImpl(pressedColour, pressedTextColour);
		} else {
			drawImpl(colour, textColour);
		}
	}

	// Draw button
	protected void drawImpl(int buttonColour, int textColour) {
		stroke(borderColour);
		strokeWeight(borderWeight);
		fill(buttonColour);
		rect(borderWeight, borderWeight, width - 2 * borderWeight, height - 2 * borderWeight, cornerRadius);

		if (text != null) {
			if (font != null) {
				textFont(font);
			}
			textAlign(CENTER, CENTER);
			textSize(fontSize);
			fill(textColour);
			text(text, width / 2 - borderWeight, height / 2 - borderWeight);
		}
	}

	// Draw for zone picker
	@Override
	protected void pickDrawImpl() {
		rect(borderWeight, borderWeight, width - 2 * borderWeight, height - 2 * borderWeight, cornerRadius);
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

		if (isButtonDown()) {
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

	/**
	 * Return the state of the button.
	 * 
	 * @return true if button is pressed and false otherwise
	 */
	public boolean isButtonDown() {
		return buttonDown;
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
	 * @param colour
	 *            colour of the button, when it is not pressed
	 * @param pressedColour
	 *            colour of the button, when it is pressed
	 * @param borderColour
	 *            colour of the button's border
	 */
	public void setColourScheme(int colour, int pressedColour, int borderColour) {
		this.colour = colour;
		this.pressedColour = pressedColour;
		this.borderColour = borderColour;
	}
}
