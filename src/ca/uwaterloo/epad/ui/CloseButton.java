package ca.uwaterloo.epad.ui;

import java.lang.reflect.Method;

import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;
import processing.core.PShape;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;

public class CloseButton extends Zone {
	public int backgroundColour = Application.deleteColour;
	public int iconColour = Application.backgroundColour;
	protected static PShape icon;
	protected boolean buttonDown = false;

	public CloseButton(int x, int y, int width, int height) {
		super(x, y, width, height);
		
		if (icon == null) {
			icon = applet.loadShape(Settings.dataFolder + "vector\\x.svg");
			icon.disableStyle();
		}
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
		// try to use parent's close() method
		try {
			Class<?> c = parent.getClass();
			Method closeMethod = c.getDeclaredMethod("close");
			closeMethod.invoke(parent);
		} catch (Exception e) {
			if (parent != null) {
				// if it doesn't exist, simply remove parent from the client
				TouchClient.remove(parent);
			} else {
				// if the close button is on the top layer, use it to exit application
				applet.exit();
			}
		}
	}
}