package ca.uwaterloo.epad.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.painting.Stroke;
import ca.uwaterloo.epad.util.Settings;

import processing.core.PGraphics;
import processing.core.PImage;
import vialab.SMT.Touch;
import vialab.SMT.Zone;

public class Canvas extends Zone implements ActionListener {
	public int backgroundColour = 255;

	private HashMap<Long, Stroke> strokes;
	private PGraphics drawing;
	private PImage overlayImage;
	public boolean useOverlay = true;
	private boolean isDrawerOpen;

	public Canvas(int x, int y, int width, int height, int backgroundColour) {
		super(0, 0, width, height);
		translate(x, y);
		isDrawerOpen = false;
		this.backgroundColour = backgroundColour;

		overlayImage = applet.loadImage(Settings.dataFolder + "images\\house.png");

		drawing = applet.createGraphics(width, height, P2D);
		clear();
	}

	protected void drawImpl() {
		if (isDrawerOpen) {
			// Draw border to indicate the moving state
			stroke(0xFF0099CC);
			strokeWeight(2);
			fill(0x33000000);
			rect(-30, -30, width + 60, height + 60, 30);
		}
		
		noStroke();
		fill(backgroundColour);
		rect(0, 0, width, height);

		image(drawing, 0, 0, width, height);
		
		if (useOverlay)
			image(overlayImage, 0, 0, width, height);
	}

	protected void pickDrawImpl() {
		if (isDrawerOpen)
			rect(-30, -30, width + 60, height + 60, 30);
		else
			rect(0, 0, width, height);
	}

	protected void touchImpl() {
		if (isDrawerOpen)
			rst();
		else
			addStroke();
		
		Application.setActionPerformed();
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
	
	public void clear() {
		drawing.beginDraw();
		drawing.noStroke();
		drawing.fill(backgroundColour);
		drawing.rect(0, 0, width, height);
		drawing.endDraw();
		
		if (strokes == null)
			strokes = new HashMap<Long, Stroke>();
		else
			strokes.clear();
	}
	
	public void clearAndLoad(String filename) {
		PImage underlayImage = applet.loadImage(filename);
		
		drawing.beginDraw();
		drawing.noStroke();
		drawing.fill(backgroundColour);
		drawing.rect(0, 0, width, height);
		drawing.image(underlayImage, 0, 0, width, height);
		drawing.endDraw();
		
		if (strokes == null)
			strokes = new HashMap<Long, Stroke>();
		else
			strokes.clear();
	}
	
	public void toggleOverlay() {
		useOverlay = !useOverlay;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals(Drawer.OPEN))
			isDrawerOpen = true;
		else if (event.getActionCommand().equals(Drawer.CLOSED))
			isDrawerOpen = false;
	}
}
