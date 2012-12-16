package ca.uwaterloo.epad.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import processing.core.PGraphics;
import processing.core.PImage;
import vialab.SMT.Touch;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.painting.Stroke;

public class Canvas extends Zone {
	private static final int PAINTING = 0;
	private static final int MOVING = 1;

	public int state;
	public int backgroundColour = 255;

	private HashMap<Long, Stroke> strokes;
	private PGraphics drawing;
	private PImage overlayImage;

	public Canvas(int x, int y, int width, int height) {
		super(x, y, width, height);
		state = PAINTING;

		overlayImage = applet.loadImage("data\\images\\house.png");

		drawing = applet.createGraphics(width, height, P2D);

		drawing.beginDraw();
		drawing.noStroke();
		drawing.fill(backgroundColour);
		drawing.rect(0, 0, width, height);
		drawing.endDraw();

		strokes = new HashMap<Long, Stroke>();
	}

	protected void drawImpl() {
		if (state == MOVING) {
			// Draw border to indicate the moving state
			stroke(0);
			strokeWeight(2);
			fill(0x88333333);
			rect(-30, -30, width + 60, height + 60, 30);
		}

		noStroke();
		fill(backgroundColour);
		rect(0, 0, width, height);

		image(drawing, 0, 0, width, height);
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
	
	public PGraphics getDrawing() {
		return drawing;
	}
}
