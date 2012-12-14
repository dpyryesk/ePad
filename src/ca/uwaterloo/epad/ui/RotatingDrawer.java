package ca.uwaterloo.epad.ui;

import processing.core.PApplet;
import processing.core.PVector;
import vialab.SMT.Touch;
import vialab.SMT.Zone;

public class RotatingDrawer extends Zone {
	private boolean isOpen;
	private int position;
	private int diameter;
	private float angle;
	private int dragXMin, dragXMax, dragYMin, dragYMax;
	private boolean dragX, dragY;
	private RotatingContainer container;
	
	public static RotatingDrawer makeLeftDrawer(PApplet parent) {
		RotatingDrawer instance = new RotatingDrawer(-parent.height*2, -parent.height/2, parent.height*2, LEFT);
		
		instance.container = new RotatingContainer(instance);
		instance.add(instance.container);
		
		return instance;
	}
	
	public static RotatingDrawer makeRightDrawer(PApplet parent) {
		RotatingDrawer instance = new RotatingDrawer(parent.width, -parent.height/2, parent.height*2, RIGHT);
		
		instance.container = new RotatingContainer(instance);
		instance.add(instance.container);
		
		return instance;
	}
	
	private RotatingDrawer (int x, int y, int diameter, int position) {
		super(x, y, diameter, diameter);
		isOpen = false;
		this.position = position;
		this.diameter = diameter;
		
		if (position == LEFT) {
			angle = -HALF_PI;
			dragX = true;
			dragY = false;
			dragXMin = x;
			dragXMax = x + width + width/4;
			dragYMin = Integer.MIN_VALUE;
			dragYMax = Integer.MAX_VALUE;
		} else if (position == RIGHT) {
			angle = HALF_PI;
			dragX = true;
			dragY = false;
			dragXMin = x + width - width/4;
			dragXMax = x + width*2;
			dragYMin = Integer.MIN_VALUE;
			dragYMax = Integer.MAX_VALUE;
		}
		
		rotateAbout(angle, CENTER);
	}
	
	protected void drawImpl() {
		pushMatrix();
		
		translate(width/2, height/2);
		
		//stroke(0xFF0099CC);
		//strokeWeight(3);
		noStroke();
		fill(0xFF0099CC);
		
		ellipseMode(CENTER);
		ellipse(0, 0, diameter, diameter);
		
		translate(0, diameter/2);
		noStroke();
		triangle(-100, -7, 0, 60, 100, -7);
		
		stroke(0xFF33B5E5);
		strokeWeight(3);
		line(-100, -7, 0, 60);
		line(0, 60, 100, -7);
		
		popMatrix();
	}
	
	protected void pickDrawImpl() {
		pushMatrix();
		translate(width/2, height/2);
		ellipseMode(CENTER);
		ellipse(0, 0, diameter, diameter);
		translate(0, diameter/2);
		triangle(-100, -5, 0, 60, 100, -5);
		popMatrix();
	}
	
	protected void touchImpl() {
		drag(dragX, dragY, dragXMin, dragXMax, dragYMin, dragYMax);
		
		// figure out if the drawer is opened
		PVector p = fromZoneVector(new PVector(x, y));
		float d = 0;
		if (position == LEFT) {
			d = width/4 - (x - p.x);
		} else if (position == RIGHT) {
			d = (x - p.x) + width + width/4;
		}
		if (d > 30) {
			isOpen = true;
		} else {
			isOpen = false;
		}
	}
	
	protected void touchDownImpl(Touch touch) {
		client.putZoneOnTop(this);
	}

	public int getDiameter() {
		return diameter;
	}
	
	public int getPosition() {
		return position;
	}

	public RotatingContainer getContainer() {
		return container;
	}
	
	public boolean isOpen() {
		return isOpen;
	}
	
	public void setColourScheme(int primary, int secondary) {
		container.setColourScheme(primary, secondary);
	}
	
	public void setColourScheme(int primary, int secondary, int background) {
		container.setColourScheme(primary, secondary, background);
	}
	
	public int getPrimaryColour() {
		return container.getPrimaryColour();
	}
	
	public int getSecondaryColour() {
		return container.getSecondaryColour();
	}
	
	public int getBackgroundColour() {
		return container.getBackgroundColour();
	}
}
