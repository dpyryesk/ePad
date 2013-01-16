package ca.uwaterloo.epad.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import processing.core.PShape;
import vialab.SMT.Touch;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;

public class IconButton extends Zone {
	public int backgroundColour;
	public int iconColour;
	
	protected PShape icon;
	protected boolean buttonDown = false;
	protected Method pressMethod;
	protected Object pressMethodObject;

	public IconButton(int x, int y, int width, int height, String iconName, int backgroundColour, int iconColour) {
		super(x, y, width, height);
		
		icon = applet.loadShape(Settings.dataFolder + "vector\\" + iconName + ".svg");
		icon.disableStyle();
		
		this.backgroundColour = backgroundColour;
		this.iconColour = iconColour;
	}
	
	protected void drawImpl() {
		noStroke();
		fill(backgroundColour);
		ellipseMode(CORNER);
		ellipse(0, 0, width, height);
		
		fill(iconColour);
		shapeMode(CENTER);
		shape(icon, width/2, height/2, width*0.75f, height*0.75f);
	}
	
	protected void pickDrawImpl() {
		ellipseMode(CORNER);
		ellipse(0, 0, width, height);
	}
	
	public void touchImpl() {
		Application.setActionPerformed();
	}
	
	public void touchUp(Touch touch) {
		setButtonDown();
		super.touchUp(touch);

		if (buttonDown) {
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