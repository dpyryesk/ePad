package ca.uwaterloo.epad.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import processing.core.PFont;
import vialab.SMT.Touch;
import vialab.SMT.Zone;

public class Button extends Zone {
	protected int fontSize;
	protected String text;
	protected PFont font;
	protected float cornerRadius = 12;
	protected int colour = 0xFF99CC00;
	protected int pressedColour = 0xFF669900;
	protected int borderWeight = 2;
	protected int borderColour = 0xFF669900;
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
	}
	
	public void touchUp(Touch touch) {
		setButtonDown();
		super.touchUp(touch);

		if (isButtonDown()) {
			invokePress();
		}
		buttonDown = false;
	}

	@Override
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
	
	public void setPressMethodByName(String methodName, Object obj) {
		Class<?> c = obj.getClass();
		pressMethodObject = obj;
		try {
			pressMethod = c.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
