package ca.uwaterloo.epad.ui;

import processing.core.PImage;
import vialab.SMT.Zone;


public class Canvas extends Zone {
	private static final int PAINTING = 0;
	private static final int MOVING = 1;
	
	public int state;
	
	private PImage textureImage;
	//public ResizeBar leftBar;
	
	public Canvas(int x, int y, int width, int height) {
		super(x, y, width, height);
		state = MOVING;
		
		textureImage = applet.loadImage("data/textures/deep sea.jpg");
		
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
		rect(0, 0, width, height, 10);
		
		pg.textureMode(NORMAL);
		pg.textureWrap(REPEAT);
		
		beginShape();
		texture(textureImage);
		vertex(0, 0, 0, 0);
		vertex(0, height, 0, 2);
		vertex(width, height, 2, 2);
		vertex(width, 0, 2, 0);
		endShape();
		
		//image(textureImage, -100, -100);
	}
	
	protected void pickDrawImpl() {
		if (state == MOVING) {
			rect(-30, -30, width+60, height+60, 30);
		} else {
			rect(0, 0, width, height);
		}
	}
	
	protected void touchImpl() {
		if (state == MOVING) {
			rst();
		}
	}
}
