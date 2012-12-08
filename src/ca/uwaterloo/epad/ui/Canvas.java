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
	
	private HashMap<Long, Stroke> strokes;
	private PGraphics drawing;
	private PImage textureImage;
	//public ResizeBar leftBar;
	
	public Canvas(int x, int y, int width, int height) {
		super(x, y, width, height);
		state = PAINTING;
		
		textureImage = applet.loadImage("data/textures/eggshell.jpg");
		
		drawing = applet.createGraphics(width, height, P2D);
		drawing.smooth();
		drawing.beginDraw();
		drawing.beginShape();
		drawing.textureMode(IMAGE);
		drawing.textureWrap(REPEAT);
		drawing.texture(textureImage);
		drawing.noStroke();
		drawing.vertex(0, 0, 0, 0);
		drawing.vertex(0, height, 0, height);
		drawing.vertex(width, height, width, height);
		drawing.vertex(width, 0, width, 0);
		drawing.endShape();
		drawing.endDraw();
		
		strokes = new HashMap<Long, Stroke>();
		
		//leftBar = new ResizeBar(this, ResizeBar.LEFT);
		//add(leftBar);
	}
	
	protected void drawImpl() {
		if (state == MOVING) {
			// Draw border to indicate the moving state
			stroke(0);
			strokeWeight(2);
			fill(0x88333333);
			rect(-30, -30, width+60, height+60, 30);
		}
		
		noStroke();
		fill(255);
		rect(0, 0, width, height);
		
		/*
		pg.textureMode(NORMAL);
		pg.textureWrap(REPEAT);
		
		beginShape();
		texture(textureImage);
		vertex(0, 0, 0, 0);
		vertex(0, height, 0, 2);
		vertex(width, height, 2, 2);
		vertex(width, 0, 2, 0);
		endShape();
		*/
		
		image(drawing, 0, 0, width, height);
		//blend(drawing, 0, 0, width, height, 0, 0, width, height, OVERLAY);
	}
	
	protected void pickDrawImpl() {
		if (state == MOVING) {
			rect(-30, -30, width+60, height+60, 30);
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
		if (!activeTouches.isEmpty()) {
			Set<Long> keys = activeTouches.keySet();
			Iterator<Long> it = keys.iterator();
			
			while(it.hasNext()) {
				Long id = it.next();
				Touch t = activeTouches.get(id);
				
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
}
