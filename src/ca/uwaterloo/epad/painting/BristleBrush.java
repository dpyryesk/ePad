package ca.uwaterloo.epad.painting;

import processing.core.PGraphics;
import processing.core.PShape;

public class BristleBrush extends Brush {
	protected int size;
	private PShape brushShape;
	
	public BristleBrush(int size) {
		super(size, size);
		this.size = size;
		name = "BristleBrush " + size;
	}
	
	public BristleBrush(BristleBrush original) {
		super(original);
		size = original.size;
		name = "BristleBrush " + size;
		
		try {
			brushShape = applet.loadShape("..\\data\\vector\\brush1.svg");
			brushShape.disableStyle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void drawItem() {
		fill(secondaryColour);
		noStroke();
		rectMode(CENTER);
		rect(width/2, height/2, 100, 100);
		line(width/2-10, height/2, width/2+10, height/2);
		line(width/2, height/2-10, width/2, height/2+10);
		
		fill(0);
		text(name, 30, 30);
	}
	
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0) return;
		
		StrokePoint p = s.getPath().get(length-1);
		
		g.beginDraw();
		g.noStroke();
		g.fill(colour);
		g.shapeMode(CENTER);
		g.shape(brushShape, p.x, p.y, size, size);
		g.endDraw();
	}

}
