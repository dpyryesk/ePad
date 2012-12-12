package ca.uwaterloo.epad.painting;

import processing.core.PConstants;
import processing.core.PGraphics;

public class Eraser extends Brush {
	protected int size;
	private int canvasColour;

	public Eraser(int size, int canvasColour) {
		super(size, size);
		this.size = size;
		this.canvasColour = canvasColour;
		name = "Eraser " + size;
	}
	
	public Eraser(Eraser original) {
		super(original);
		size = original.size;
		canvasColour = original.canvasColour;
		name = original.name;
	}
	
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0) return;
		if (length == 1) {
			StrokePoint p = s.getPath().get(length-1);
			
			g.beginDraw();
			g.noStroke();
			g.fill(canvasColour);
			g.ellipseMode(PConstants.CENTER);
			g.ellipse(p.x, p.y, size, size);
			g.endDraw();
		} else {
			StrokePoint from = s.getPath().get(length-2);
			StrokePoint to = s.getPath().get(length-1);
			
			g.beginDraw();
			g.strokeJoin(PConstants.ROUND);
			g.strokeCap(PConstants.ROUND);
			g.stroke(canvasColour);
			g.strokeWeight(size);
			g.line(from.x, from.y, to.x, to.y);
			g.endDraw();
		}
	}

}
