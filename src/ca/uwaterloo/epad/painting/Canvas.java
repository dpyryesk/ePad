package ca.uwaterloo.epad.painting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ca.uwaterloo.epad.Application;

import processing.core.PGraphics;
import processing.core.PImage;
import vialab.SMT.Touch;
import vialab.SMT.Zone;

public class Canvas extends Zone {
	private static final int PAINTING = 0;
	private static final int MOVING = 1;

	public int backgroundColour = 255;

	private HashMap<Long, Stroke> strokes;
	private PGraphics drawing;
	private PImage textureImage, overlayImage;
	private boolean useTexture = false;
	private boolean useOverlay = true;
	private int state;

	public Canvas(int x, int y, int width, int height, int backgroundColour) {
		super(0, 0, width, height);
		translate(x, y);
		state = PAINTING;
		this.backgroundColour = backgroundColour;
		
		textureImage = applet.loadImage("data\\textures\\eggshell.jpg");
		overlayImage = applet.loadImage("data\\images\\house.png");

		drawing = applet.createGraphics(width, height, P2D);

		drawing.beginDraw();
		drawing.background(0, 0);
		drawing.noStroke();
		drawing.fill(backgroundColour);
		drawing.rect(0, 0, width, height);
		drawing.endDraw();

		strokes = new HashMap<Long, Stroke>();
	}

	protected void drawImpl() {
		if (Application.isDrawerOpen()) {
			// Draw border to indicate the moving state
			stroke(0xFF0099CC);
			strokeWeight(2);
			fill(0x33000000);
			rect(-30, -30, width + 60, height + 60, 30);
			state = MOVING;
		} else {
			state = PAINTING;
		}
		
		if (useTexture) {
			beginShape();
			textureMode(IMAGE);
			texture(textureImage);
			noStroke();
			vertex(0, 0, 0, 0);
			vertex(0, height, 0, height);
			vertex(width, height, width, height);
			vertex(width, 0, width, 0);
			endShape();
		} else {
			noStroke();
			fill(backgroundColour);
			rect(0, 0, width, height);
		}

		image(drawing, 0, 0, width, height);
		
		if (useOverlay)
			image(overlayImage, 0, 0, width, height);
	}

	protected void pickDrawImpl() {
		if (state == MOVING) {
			rect(-30, -30, width + 60, height + 60, 30);
		} else if (state == PAINTING) {
			rect(0, 0, width, height);
		}
	}

	protected void touchImpl() {
		if (state == MOVING) {
			rst();
		} else if (state == PAINTING) {
			addStroke();
		}
	}

	protected void addStroke() {
		if (!getTouchMap().isEmpty()) {
			Set<Long> keys = getTouchMap().keySet();
			Iterator<Long> it = keys.iterator();

			while (it.hasNext()) {
				Long id = it.next();
				Touch t = getTouchMap().get(id);

				if (strokes.containsKey(id)) {
					// update stroke
					Stroke s = strokes.get(id);
					s.update(t);
					s.render(drawing);
				} else {
					// new stroke
					Stroke s = new Stroke(t, this);
					strokes.put(id, s);
					s.render(drawing);
				}
			}
		}
	}
	
	public PImage getDrawing() {
		PGraphics result = applet.createGraphics(width, height, P2D);
		result.beginDraw();
		result.image(drawing, 0, 0, width, height);
		if (useOverlay)
			result.blend(overlayImage, 0, 0, width, height, 0, 0, width, height, DARKEST);
		result.endDraw();
		
		return result.get();
	}
}
