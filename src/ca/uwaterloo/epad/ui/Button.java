package ca.uwaterloo.epad.ui;

import processing.core.PFont;
import vialab.SMT.ButtonZone;

public class Button extends ButtonZone {
	public Button(int x, int y, int width, int height, String text, int fontSize,
			PFont font, float angle) {
		super(null, x, y, width, height);
		setText(text);
		setFontSize(fontSize);
		setFont(font);
		setAngle(angle);
	}
	
	@Override
	protected void pressImpl() {
		applet.exit();
	}
}
