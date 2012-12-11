package ca.uwaterloo.epad.painting;

import java.util.ArrayList;

import processing.core.PGraphics;

public class BristleBrush extends Brush {
	protected int size;
	//private PShape brushShape;
	private ArrayList<Bristle> bristleList;
	private int numBristles = 100;
	private float bristleSizeMin = 2;
	private float bristleSizeMax = 5;
	
	public BristleBrush(int size) {
		super(size, size);
		this.size = size;
		name = "BristleBrush " + size;
	}
	
	public BristleBrush(BristleBrush original) {
		super(original);
		size = original.size;
		numBristles = size * 3;
		name = "BristleBrush " + size;
		
		//brushShape = applet.loadShape("..\\data\\vector\\brush1.svg");
		//brushShape.disableStyle();
		
		bristleList = new ArrayList<Bristle>();
		makeBristles();
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
		if (length == 1) makeBristles(); // make new pattern of bristles
		
		// wash colour out as stroke gets longer
		/*
		int alpha = 255 - length*3;
		if (alpha <= 0) alpha = 1;
		
		int red = (colour >> 16) & 0xFF;
		int green = (colour >> 8) & 0xFF;
		int blue = colour & 0xFF;
		*/
		
		StrokePoint p = s.getPath().get(length-1);
		
		g.beginDraw();
		g.noStroke();
		//g.fill(red, green, blue, alpha);
		g.fill(colour);
		g.translate(p.x, p.y);
		for (Bristle b : bristleList) {
			b.draw(g);
		}
		//g.shapeMode(CENTER);
		//g.shape(brushShape, p.x, p.y, size, size);
		g.endDraw();
	}
	
	private void makeBristles() {
		if (bristleList == null) {
			System.out.println("BristleBrush: bristleList is not initialized");
			return;
		} else {
			bristleList.clear();
		}
		
		float x, y, width;
		for (int i=0; i<numBristles; i++) {
			x = applet.random(-size/2, size/2);
			y = applet.random(-size/2, size/2);
			if (Math.sqrt(x*x + y*y) > size/2) {
				i--;
			} else {
				width = applet.random(bristleSizeMin, bristleSizeMax);
				bristleList.add(new Bristle(x, y, width));
			}
		}
	}
	
	private class Bristle {
		private float x, y, width;
		
		public Bristle(float x, float y, float width) {
			this.x = x;
			this.y = y;
			this.width = width;
		}
		
		public void draw(PGraphics g) {
			g.ellipse(x, y, width, width);
		}
	}

}
