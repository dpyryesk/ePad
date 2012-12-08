package ca.uwaterloo.epad.painting;

import java.util.ArrayList;
import java.util.Iterator;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

public class SpiderBrush extends Brush {
	private ArrayList<StrokePoint> pointList = new ArrayList<StrokePoint>();
	private float minDistance = 10;
	private float connectionRadius = 150;
	private float lineWeight = 1;
	private float lineAlpha = 128;
	private float lineAlpha2 = 50;

	public SpiderBrush() {
		super(1, 1);
		name = "SpiderBrush";
	}

	public SpiderBrush(SpiderBrush original) {
		super(original);
		name = "SpiderBrush";
	}

	protected void drawItem() {
		fill(secondaryColour);
		noStroke();
		rectMode(CENTER);
		rect(width / 2, height / 2, 100, 100);
		line(width / 2 - 10, height / 2, width / 2 + 10, height / 2);
		line(width / 2, height / 2 - 10, width / 2, height / 2 + 10);

		fill(0);
		text(name, 30, 30);
	}

	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0)
			return;
		if (length == 1) {
			StrokePoint p = s.getPath().get(length - 1);
			pointList.add(p);

			g.beginDraw();
			g.noStroke();
			g.fill(colour, lineAlpha);
			g.ellipseMode(PConstants.CENTER);
			g.ellipse(p.x, p.y, lineWeight, lineWeight);
			g.endDraw();
		} else {
			StrokePoint from = pointList.get(pointList.size()-1);
			StrokePoint to = s.getPath().get(length - 1);
			
			if (from.dist(to) > minDistance) {
				pointList.add(to);
				
				g.beginDraw();
				g.strokeWeight(lineWeight);
				g.stroke(colour, lineAlpha);
				g.strokeCap(ROUND);
				g.noFill();
				//g.tint(255, imageAlpha);
				g.line(from.x, from.y, to.x, to.y);
				
				g.stroke(colour, lineAlpha2);
				Iterator<StrokePoint> it = pointList.iterator();
				while(it.hasNext()) {
					StrokePoint p = it.next();
					if (PApplet.dist(p.x, p.y, to.x, to.y) <= connectionRadius)
						g.line(p.x, p.y, to.x, to.y);
				}
				
				g.endDraw();
			} else {
				from = s.getPath().get(length - 2);
				g.beginDraw();
				g.strokeWeight(lineWeight);
				g.stroke(colour, lineAlpha);
				g.strokeCap(ROUND);
				g.noFill();
				//g.tint(255, imageAlpha);
				g.line(from.x, from.y, to.x, to.y);
				g.endDraw();
			}
		}
	}
}
