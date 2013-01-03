package ca.uwaterloo.epad.prompting;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.util.Settings;

public class PromptPopup {
	public static final int LOCATION_AUTO = 0;
	public static final int LOCATION_TOP_LEFT = 1;
	public static final int LOCATION_TOP_RIGHT = 2;
	public static final int LOCATION_BOTTOM_LEFT = 3;
	public static final int LOCATION_BOTTOM_RIGHT = 4;

	protected static PFont font;

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
	protected int location = LOCATION_AUTO;
	protected String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras accumsan iaculis vehicula.";
	protected boolean showIcon = true;
	protected boolean showText = true;
	protected boolean readText = true;

	// Colours
	protected int backgroundColour = 255;
	protected int hightlightColour = 0xFFFFBB33;
	protected int iconColour = 0xFFFF8800;
	protected int textColour = 0;

	protected PShape icon;

	private float lx = 0;
	private float ly = 0;
	private float cx = 0;
	private float cy = 0;
	private PImage cache;
	private PApplet applet;

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
		applet.pushMatrix();
		applet.pushStyle();
		applet.translate(x, y);

		// draw connecting line
		if (showText) {
			applet.stroke(hightlightColour);
			applet.strokeWeight(3);
			applet.line(0, 0, lx, ly);
		}

		// draw background for icon
		if (showIcon) {
			applet.fill(backgroundColour);
			applet.ellipseMode(PConstants.CENTER);
			applet.ellipse(0, 0, iconBackgroundRadius, iconBackgroundRadius);

			// draw icon
			if (icon != null) {
				applet.fill(iconColour);
				applet.shape(icon, -iconSize / 2, -iconSize / 2);
			}
		}

		if (showText)
			applet.image(cache, cx, cy);

		applet.popStyle();
		applet.popMatrix();
	}

	protected void pickDrawImpl() {
		applet.rectMode(PConstants.CORNER);
		applet.rect(cx, cy, messageWidth, messageHeight);
	}

	public void touch() {
		// TouchClient.remove(this);
	}

	public void init() {
		applet = PromptManager.parent;

		if (applet == null) {
			System.err.println("Error: Unable to instantiate PromptPopup before PromptManager.init().");
			return;
		}

		font = applet.createFont(fontName, fontSize);

		try {
			icon = applet.loadShape(Settings.dataFolder + "vector\\cue\\" + iconName + ".svg");
			icon.disableStyle();
			icon.scale(iconSize / 30f);
		} catch (Exception e) {
			icon = null;
		}

		if (location == LOCATION_AUTO)
			calculateLocation();

		if (showText) {
			calculateTextLocation();

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

			// create an action zone
			TouchClient.add(new PromptZone(this));
		}
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
			lx = xOffset;
			ly = -yOffset;
			cx = xOffset;
			cy = -yOffset - messageHeight / 2;
		} else if (location == LOCATION_TOP_LEFT) {
			lx = -xOffset;
			ly = -yOffset;
			cx = -xOffset - messageWidth;
			cy = -yOffset - messageHeight / 2;
		} else if (location == LOCATION_BOTTOM_RIGHT) {
			lx = xOffset;
			ly = yOffset + messageHeight / 2;
			cx = xOffset;
			cy = yOffset;
		} else if (location == LOCATION_BOTTOM_LEFT) {
			lx = -xOffset;
			ly = yOffset + messageHeight / 2;
			cx = -xOffset - messageWidth;
			cy = yOffset;
		}
	}

	private class PromptZone extends Zone {
		private PromptPopup pp;

		public PromptZone(PromptPopup pp) {
			super(pp.x, pp.y, messageWidth, messageHeight);
			this.pp = pp;
		}

		protected void drawImpl() {
			rectMode(PConstants.CORNER);
			noStroke();
			noFill();
			rect(cx, cy, width, height);
		}

		protected void pickDrawImpl() {
			rectMode(PConstants.CORNER);
			rect(cx, cy, width, height);
		}

		protected void touchImpl() {
			PromptManager.remove(pp);
			TouchClient.remove(this);
		}
	}
}
