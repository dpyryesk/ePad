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

import org.apache.log4j.Logger;

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

/**
 * This class represents a prompt pop-up message. It uses a set of icons located
 * in folder /data/vector/cue to display multi-touch gestures (these icons were
 * developed by P.J. Onori and can be found here: <a
 * href="http://somerandomdude.com/work/cue/"
 * >http://somerandomdude.com/work/cue/</a>).
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see PromptManager
 */
public class PromptPopup {
	private static final Logger LOGGER = Logger.getLogger(PromptPopup.class);

	// Location constants
	public static final int LOCATION_TOP_LEFT = 1;
	public static final int LOCATION_TOP_RIGHT = 2;
	public static final int LOCATION_BOTTOM_LEFT = 3;
	public static final int LOCATION_BOTTOM_RIGHT = 4;

	// Prompt parameters
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
	protected String text = "";
	protected boolean showIcon = true;
	protected boolean showText = true;

	// Colours
	public int backgroundColour = 255;
	public int hightlightColour = 0xFFFFBB33;
	public int iconColour = 0xFFFF8800;
	public int textColour = 0;

	/**
	 * Tween object that controls fading in and out.
	 */
	private Tween alphaTween;

	/**
	 * Time-to-live timer that indicated when a prompt must be disposed of.
	 */
	private Timer ttlTimer;

	/**
	 * Flag that indicates that the prompt has been disposed and is currently
	 * fading out.
	 */
	private boolean isDisposing = false;

	// Misc. variables
	protected PShape icon;
	protected static PFont font;
	private float cx = 0;
	private float cy = 0;
	private PImage cache;
	private PApplet applet;

	/**
	 * Make a prompt with the given coordinates and icon.
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 * @param iconName
	 *            name of the icon file <i>not including the extension .svg</i>
	 */
	public PromptPopup(int x, int y, String iconName) {
		this(x, y, iconName, null);
		showText = false;
	}

	/**
	 * Make a prompt with the given coordinates, icon and message.
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 * @param iconName
	 *            name of the icon file <i>not including the extension .svg</i>
	 * @param text
	 *            prompt message
	 */
	public PromptPopup(int x, int y, String iconName, String text) {
		this.x = x;
		this.y = y;

		this.iconName = iconName;
		this.text = text;

		// Save the pointer to the parent applet
		applet = PromptManager.parent;

		if (applet == null) {
			System.err.println("Error: Must call PromptManager.init() before instantiating a PromptPopup.");
			return;
		}

		// Initialise the tween for the fade-in effect. It will change the value
		// of alpha from 10 to 255 in 0.5 seconds
		alphaTween = new Tween(10, 255, 500);

		// Create the font
		font = applet.createFont(fontName, fontSize);

		// Load specified the icon
		loadIcon();

		// Calculate the location of the message and create cache
		if (showText) {
			calculateTextLocation();
			createCache();
		}

		// Initialise the TTL timer
		ttlTimer = new Timer(Settings.promptTTL);
	}

	/**
	 * Draw the prompt using the given parameters. PropmtManager must be
	 * initialised <i>before</i> this function is called.
	 * 
	 * @see PromptManager#init(PApplet)
	 */
	public void draw() {
		if (applet == null) {
			System.err.println("Error: Must call PromptManager.init() before instantiating a PromptPopup.");
			return;
		}

		// Check the TTL timer and dispose of the prompt if it has ran out
		if (ttlTimer.isTimeOut())
			dispose();

		applet.pushMatrix();
		applet.pushStyle();

		applet.translate(x, y);

		// Draw background for icon
		if (showIcon) {
			// Draw the icon
			if (icon != null) {
				applet.fill(iconColour);
				applet.shape(icon, -iconSize / 2, -iconSize / 2);
			}
		}

		// Draw the cache image with the prompt message
		if (showText) {
			applet.tint(255, alphaTween.getValue());
			applet.image(cache, cx, cy);
		}

		applet.popStyle();
		applet.popMatrix();
	}

	/**
	 * Pause the prompt.
	 */
	public void pause() {
		ttlTimer.pause();
	}

	/**
	 * Resume the prompt.
	 */
	public void resume() {
		ttlTimer.resume();
	}

	/**
	 * Start the disposal of the prompt. The prompt will gradually fade out and
	 * then will get removed by the draw() function of PromptManager.
	 * 
	 * @see PromptManager#draw()
	 */
	public void dispose() {
		if (isDisposing)
			return;

		isDisposing = true;
		alphaTween = new Tween(255, 0, 700);
	}

	// Load the specified icon
	private void loadIcon() {
		String filename = Settings.dataFolder + "vector\\cue\\" + iconName + ".svg";
		icon = applet.loadShape(filename);
		if (icon == null)
			LOGGER.error("Failed to load shape: " + filename);
		else {
			icon.disableStyle();
			icon.scale(iconSize / 30f);
		}
	}

	// Create the cache for faster processing
	private void createCache() {
		// Create cached image of the message
		PGraphics tempG = applet.createGraphics(messageWidth, messageHeight, PConstants.JAVA2D);

		tempG.beginDraw();
		tempG.smooth();
		tempG.background(0);

		// Draw background for text
		tempG.rectMode(PConstants.CORNER);
		tempG.fill(backgroundColour);
		tempG.stroke(hightlightColour);
		tempG.strokeWeight(3);
		tempG.rect(0, 0, messageWidth, messageHeight);

		// Draw text highlight
		tempG.fill(hightlightColour);
		if (location == LOCATION_TOP_LEFT || location == LOCATION_BOTTOM_LEFT)
			tempG.rect(messageWidth - hightlightWidth, 0, hightlightWidth, messageHeight);
		else if (location == LOCATION_TOP_RIGHT || location == LOCATION_BOTTOM_RIGHT)
			tempG.rect(0, 0, hightlightWidth, messageHeight);

		// Draw text
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

	// Calculate the location of the text relative to the icon
	private void calculateTextLocation() {
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

	/**
	 * Change the coordinates of the prompt to the given x and y.
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 */
	public void setCoordinates(int x, int y) {
		this.x = x;
		this.y = y;

		if (showText)
			calculateTextLocation();
	}

	/**
	 * Change the coordinates of the prompt to the given x and y.
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 */
	public void setCoordinates(float x, float y) {
		setCoordinates((int) x, (int) y);
	}

	/**
	 * Change the coordinates of the prompt to the coordinates of the vector.
	 * 
	 * @param v
	 *            vector
	 */
	public void setCoordinates(PVector v) {
		setCoordinates(v.x, v.y);
	}

	/**
	 * Change the icon to the provided one.
	 * 
	 * @param icon
	 *            name of the icon must match one of the .svg files in folder
	 *            /data/vector/cue and must <b>not</b> contain the extension
	 *            .svg. For example: setting icon to the string "tap" will load
	 *            shape from file <i>/data/vector/cue/tap.svg</i>.
	 */
	public void setIcon(String icon) {
		if (isDisposing)
			return;

		this.iconName = icon;
		loadIcon();
		alphaTween = new Tween(10, 255, 500);
		ttlTimer = new Timer(Settings.promptTTL);
	}

	/**
	 * Change the message to the provided text.
	 * 
	 * @param text
	 *            text of the message
	 */
	public void setText(String text) {
		if (isDisposing)
			return;

		this.text = text;
		showText = true;
		createCache();
		alphaTween = new Tween(10, 255, 700);
		ttlTimer = new Timer(Settings.promptTTL);
	}

	/**
	 * Stop showing the text.
	 */
	public void hideText() {
		showText = false;
	}

	/**
	 * Get the visibility of the prompt.
	 * 
	 * @return <b>true</b> if alpha of the prompt is equal to 0 and <b>false</b>
	 *         otherwise
	 */
	public boolean isInvisible() {
		return alphaTween.getValue() == 0;
	}
}
