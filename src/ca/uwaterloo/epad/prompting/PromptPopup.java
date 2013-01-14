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

package ca.uwaterloo.epad.prompting;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.util.Timer;
import ca.uwaterloo.epad.util.Tween;

public class PromptPopup {
	public static final int LOCATION_TOP_LEFT = 1;
	public static final int LOCATION_TOP_RIGHT = 2;
	public static final int LOCATION_BOTTOM_LEFT = 3;
	public static final int LOCATION_BOTTOM_RIGHT = 4;

	protected int x, y;
	protected String iconName = "tap";
	protected float iconSize = 75;
	protected float iconBackgroundRadius = 75;
	protected float hightlightWidth = 10;
	protected int messageWidth = 300;
	protected int messageHeight = 100;
	protected String fontName = "Arial";
	protected int fontSize = 20;
	protected float textPadding = 5;
	protected float xOffset = 75;
	protected float yOffset = 30;
	protected int location = LOCATION_BOTTOM_RIGHT;
	protected String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras accumsan iaculis vehicula.";
	protected boolean showIcon = true;
	protected boolean showText = true;
	protected boolean readText = true;

	// Colours
	public int backgroundColour = 255;
	public int hightlightColour = 0xFFFFBB33;
	public int iconColour = 0xFFFF8800;
	public int textColour = 0;

	protected PShape icon;
	protected static PFont font;
	protected Tween alphaTween;
	
	private float cx = 0;
	private float cy = 0;
	private PImage cache;
	private PApplet applet;
	private Timer ttlTimer;
	private boolean isDisposing = false;

	public PromptPopup(int x, int y) {
		this.x = x;
		this.y = y;

		init();
	}

	public PromptPopup(int x, int y, String iconName, String text) {
		this.x = x;
		this.y = y;

		this.iconName = iconName;
		this.text = text;

		init();
	}

	public void draw() {
		if (ttlTimer.isTimeOut())
			dispose();
		
		applet.pushMatrix();
		applet.pushStyle();
		applet.translate(x, y);

		// draw background for icon
		if (showIcon) {

			// draw icon
			if (icon != null) {
				applet.fill(iconColour);
				applet.shape(icon, -iconSize / 2, -iconSize / 2);
			}
		}

		if (showText) {
			applet.tint(255, alphaTween.getValue());
			applet.image(cache, cx, cy);
		}

		applet.popStyle();
		applet.popMatrix();
	}

	public void init() {
		applet = PromptManager.parent;
		alphaTween = new Tween(10, 255, 500);

		if (applet == null) {
			System.err.println("Error: Unable to instantiate PromptPopup before PromptManager.init().");
			return;
		}

		font = applet.createFont(fontName, fontSize);

		loadIcon();
		
		calculateLocation();

		if (showText) {
			calculateTextLocation();
			createCache();
		}
		
		ttlTimer = new Timer(Settings.promptTTL);
	}
	
	public void dispose() {
		if (isDisposing) return;
		
		isDisposing = true;
		alphaTween = new Tween(255, 0, 700);
	}
	
	private void loadIcon() {
		try {
			icon = applet.loadShape(Settings.dataFolder + "vector\\cue\\" + iconName + ".svg");
			icon.disableStyle();
			icon.scale(iconSize / 30f);
		} catch (Exception e) {
			icon = null;
		}
	}

	private void createCache() {
		// create cached image of the message
		PGraphics tempG = applet.createGraphics(messageWidth, messageHeight, PConstants.JAVA2D);

		tempG.beginDraw();
		tempG.smooth();
		tempG.background(0);

		// draw background for text
		tempG.rectMode(PConstants.CORNER);
		tempG.fill(backgroundColour);
		tempG.stroke(hightlightColour);
		tempG.strokeWeight(3);
		tempG.rect(0, 0, messageWidth, messageHeight);

		// draw text highlight
		tempG.fill(hightlightColour);
		if (location == LOCATION_TOP_LEFT || location == LOCATION_BOTTOM_LEFT)
			tempG.rect(messageWidth - hightlightWidth, 0, hightlightWidth, messageHeight);
		else if (location == LOCATION_TOP_RIGHT || location == LOCATION_BOTTOM_RIGHT)
			tempG.rect(0, 0, hightlightWidth, messageHeight);

		// draw text
		tempG.fill(textColour);
		tempG.textFont(font, fontSize);
		tempG.textAlign(PConstants.CENTER, PConstants.CENTER);
		if (location == LOCATION_TOP_LEFT || location == LOCATION_BOTTOM_LEFT)
			tempG.text(text, textPadding, textPadding, messageWidth - hightlightWidth - textPadding * 2, messageHeight - textPadding * 2);
		else if (location == LOCATION_TOP_RIGHT || location == LOCATION_BOTTOM_RIGHT)
			tempG.text(text, hightlightWidth + textPadding, textPadding, messageWidth - hightlightWidth - textPadding * 2, messageHeight - textPadding * 2);

		tempG.endDraw();

		cache = tempG.get();
	}

	private void calculateLocation() {
		int centerX = applet.width / 2;
		int centerY = applet.height / 2;

		if (x < centerX) {
			if (y < centerY)
				location = LOCATION_BOTTOM_RIGHT;
			else
				location = LOCATION_TOP_RIGHT;
		} else {
			if (y < centerY)
				location = LOCATION_BOTTOM_LEFT;
			else
				location = LOCATION_TOP_LEFT;
		}
	}

	private void calculateTextLocation() {
		if (location == LOCATION_TOP_RIGHT) {
			cx = xOffset;
			cy = -yOffset - messageHeight / 2;
		} else if (location == LOCATION_TOP_LEFT) {
			cx = -xOffset - messageWidth;
			cy = -yOffset - messageHeight / 2;
		} else if (location == LOCATION_BOTTOM_RIGHT) {
			cx = xOffset;
			cy = yOffset;
		} else if (location == LOCATION_BOTTOM_LEFT) {
			cx = -xOffset - messageWidth;
			cy = yOffset;
		}
	}

	public void setCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
		
		calculateLocation();

		if (showText)
			calculateTextLocation();
	}
	
	public void setCoordinates(float x, float y) {
		setCoordinates((int)x, (int)y);
	}
	
	public void setCoordinates(PVector v) {
		setCoordinates(v.x, v.y);
	}
	
	public void setIcon(String icon) {
		if (isDisposing) return;
		
		this.iconName = icon;
		loadIcon();
		alphaTween = new Tween(10, 255, 500);
		ttlTimer = new Timer(Settings.promptTTL);
	}

	public void setText(String text) {
		if (isDisposing) return;
		
		this.text = text;
		showText = true;
		createCache();
		alphaTween = new Tween(10, 255, 700);
		ttlTimer = new Timer(Settings.promptTTL);
	}
	
	public void hideText() {
		showText = false;
	}
	
	public boolean isInvisible() {
		return alphaTween.getValue() == 0;
	}
}
