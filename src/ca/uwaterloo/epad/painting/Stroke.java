package ca.uwaterloo.epad.painting;

import java.util.Vector;

import ca.uwaterloo.epad.ui.Canvas;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import vialab.SMT.Touch;


public class Stroke {
	public long id;
	Vector<StrokePoint> path;
	Canvas canvas;
	
	public Stroke(Touch t, Canvas c) {
		id = t.sessionID;
		canvas = c;
		path = new Vector<StrokePoint>();
		
		// localise touch position
		PVector v = canvas.toZoneVector(new PVector(t.x, t.y));
		t.x = (int) v.x;
		t.y = (int) v.y;
		path.add(new StrokePoint(t));
	}
	
	public void update(Touch t) {
		// localise touch position
		PVector v = canvas.toZoneVector(new PVector(t.x, t.y));
		t.x = (int) v.x;
		t.y = (int) v.y;
		path.add(new StrokePoint(t));
	}
	
	public StrokePoint getLastPoint() {
		if (path.size() == 0)
			return null;
		else return path.lastElement();
	}
	
	public void render(PGraphics g) {
		int length = path.size();
		if (length == 0) return;
		if (length == 1) {
			StrokePoint p = path.get(length-1);
			g.beginDraw();
			g.noStroke();
			g.fill(0);
			g.ellipseMode(PConstants.CENTER);
			g.line(p.x, p.y, 3, 3);
			g.endDraw();
		} else {
			StrokePoint from = path.get(length-2);
			StrokePoint to = path.get(length-1);
			
			g.beginDraw();
			g.strokeJoin(PConstants.ROUND);
			g.strokeCap(PConstants.ROUND);
			g.stroke(0);
			g.strokeWeight(3);
			g.line(from.x, from.y, to.x, to.y);
			g.endDraw();
		}
	}
}
