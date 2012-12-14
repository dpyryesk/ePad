package ca.uwaterloo.epad.painting;

import java.util.Vector;

import processing.core.PGraphics;
import processing.core.PVector;
import vialab.SMT.Touch;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.Canvas;


public class Stroke {
	public long id;
	private Vector<StrokePoint> path;
	private Canvas canvas;
	private Paint paint;
	private Brush brush;
	
	public Stroke(Touch t, Canvas c) {
		id = t.sessionID;
		canvas = c;
		path = new Vector<StrokePoint>();
		
		PVector v = canvas.toZoneVector(new PVector(0, 0));
		getPath().add(new StrokePoint(t, v));
		
		paint = Application.getPaint();
		brush = Application.getBrush();
	}
	
	public void update(Touch t) {
		PVector v = canvas.toZoneVector(new PVector(0, 0));
		getPath().add(new StrokePoint(t, v));
	}
	
	public StrokePoint getLastPoint() {
		if (getPath().size() == 0)
			return null;
		else return getPath().lastElement();
	}
	
	public void render(PGraphics g) {
		int colour = 0;
		if (paint != null)
			colour = paint.getColour();
		
		if (brush != null)
			brush.renderStroke(this, colour, g);
	}

	public Vector<StrokePoint> getPath() {
		return path;
	}
}
