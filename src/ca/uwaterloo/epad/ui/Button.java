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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ca.uwaterloo.epad.Application;

import processing.core.PFont;
import vialab.SMT.Touch;
import vialab.SMT.Zone;

public class Button extends Zone {
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
	protected boolean buttonDown = false;
	protected Method pressMethod;
	protected Object pressMethodObject;
	
	public Button(int x, int y, int width, int height, String text, int fontSize, PFont font) {
		super(x, y, width, height);
		this.text = text;
		this.fontSize = fontSize;
		this.font = font;
	}
	
	public void touchImpl() {
		Application.setActionPerformed();
	}
	
	public void touchUp(Touch touch) {
		setButtonDown();
		super.touchUp(touch);

		if (isButtonDown()) {
			invokePress();
		}
		buttonDown = false;
	}
	
	public void touchDown(Touch touch) {
		super.touchDown(touch);
		buttonDown = true;
	}
	
	protected boolean setButtonDown() {
		buttonDown = getTouches().length > 0;
		return buttonDown;
	}
	
	public void drawImpl() {
		if (buttonDown) {
			drawImpl(pressedColour, pressedTextColour);
		}
		else {
			drawImpl(colour, textColour);
		}
	}
	
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
	
	public void setColourScheme(int colour, int pressedColour, int borderColour) {
		this.colour = colour;
		this.pressedColour = pressedColour;
		this.borderColour = borderColour;
	}
	
	public boolean isButtonDown() {
		return buttonDown;
	}
	
	protected void invokePress() {
		if (pressMethod != null) {
			try {
				pressMethod.invoke(pressMethodObject);
			}
			catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setStaticPressMethod(String methodName, Class<?> c) {
		pressMethodObject = null;
		try {
			pressMethod = c.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public void setPressMethod(String methodName, Object obj) {
		Class<?> c = obj.getClass();
		pressMethodObject = obj;
		try {
			pressMethod = c.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
