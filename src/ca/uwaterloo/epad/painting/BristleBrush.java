package ca.uwaterloo.epad.painting;

import java.util.ArrayList;

import processing.core.PGraphics;

public class BristleBrush extends Brush {
	protected int diameter;
	private ArrayList<Bristle> bristleList;
	private int numBristles = 100;
	private float bristleSizeMin = 2;
	private float bristleSizeMax = 5;
	private int brushShape;
	
	public BristleBrush(int diameter) {
		super(diameter, diameter);
		this.diameter = diameter;
		name = "BristleBrush " + diameter;
		brushShape = ELLIPSE;
	}
	
	public BristleBrush(int width, int height) {
		super(width, height);
		diameter = 0;
		name = "BristleBrush " + width + " x " + height;
		brushShape = RECT;
	}
	
	public BristleBrush(BristleBrush original) {
		super(original);
		diameter = original.diameter;
		brushShape = original.brushShape;
		name = original.name;
		if (brushShape == ELLIPSE)
			numBristles = diameter * 2;
		else if (brushShape == RECT)
			numBristles = (brushWidth + brushHeight)*2;
		
		bristleList = new ArrayList<Bristle>();
		makeBristles();
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
		g.endDraw();
	}
	
	private void makeBristles() {
		if (bristleList == null) {
			System.err.println("BristleBrush: bristleList is not initialized");
			return;
		} else {
			bristleList.clear();
		}
		
		float x, y, size;
		for (int i=0; i<numBristles; i++) {
			if (brushShape == ELLIPSE) {
				x = applet.random(-diameter/2, diameter/2);
				y = applet.random(-diameter/2, diameter/2);
				if (Math.sqrt(x*x + y*y) > diameter/2) {
					i--;
				} else {
					size = applet.random(bristleSizeMin, bristleSizeMax);
					bristleList.add(new Bristle(x, y, size));
				}
			} else if (brushShape == RECT) {
				x = applet.random(-brushWidth/2, brushWidth/2);
				y = applet.random(-brushHeight/2, brushHeight/2);
				size = applet.random(bristleSizeMin, bristleSizeMax);
				bristleList.add(new Bristle(x, y, size));
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
