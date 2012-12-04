package ca.uwaterloo.epad.ui;

import processing.core.PMatrix3D;
import processing.core.PVector;
import vialab.SMT.Touch;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;

public class MoveableItem extends Zone {
	private boolean isInDrawer = false;
	private boolean isAboveTrash = false;
	
	public int primaryColour = 255;
	public int secondaryColour = 0;
	public int deleteColour = 0xFFCC0000;
	private RotatingContainer container;
	private RotatingDrawer drawer;
	
	public MoveableItem (int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public MoveableItem (int x, int y, int width, int height, PMatrix3D matrix) {
		super(x, y, width, height);
		this.name = "copy";
		this.matrix = matrix;
	}
	
	public MoveableItem (String name, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.name = name;
	}
	
	protected void drawImpl() {
		pushMatrix();
		
		if (drawer.isOpen()) {
			if (isAboveTrash)
				stroke(deleteColour);
			else
				stroke(primaryColour);
			strokeWeight(2);
		} else {
			noStroke();
		}
		
		fill(0x22000000);
		ellipseMode(CENTER);
		ellipse(width/2, height/2, width, height);
		
		fill(secondaryColour);
		noStroke();
		rectMode(CENTER);
		rect(width/2, height/2, 100, 100);
		line(width/2-10, height/2, width/2+10, height/2);
		line(width/2, height/2-10, width/2, height/2+10);
		
		fill(0);
		text(name, 30, 30);
		
		if (!isInDrawer && drawer.isOpen()) {
			fill(primaryColour);
			ellipse(0, 0, 30, 30);
			
			if (isAboveTrash) {
				//draw delete icon
				Application.deleteIcon.disableStyle();
				fill(deleteColour);
				shape(Application.deleteIcon, -15, -15, 30, 30);
			} else {
				//draw move icon
				shape(Application.moveIcon, -15, -15, 30, 30);
			}
			
		}
		
		popMatrix();
	}
	
	protected void pickDrawImpl() {
		ellipseMode(CENTER);
		ellipse(width/2, height/2, width, height);
		if (!isInDrawer && drawer.isOpen()) {
			ellipse(0, 0, 30, 30);
		}
	}
	
	
	protected void touchImpl() {
		if (!isInDrawer && drawer.isOpen()) {
			rst();
			
			//check if item is above a trash can
			PVector drawerCetre = drawer.getCentre();
			PVector centre = getCentre();
			float d = drawerCetre.dist(centre) - drawer.getDiameter()/2;
			isAboveTrash = d < 0;
		}
	}
	
	protected void touchDownImpl(Touch touch) {
		if (drawer.isOpen()) {
			if (isInDrawer) {
				client.add(copyAndAssignTouch(this, touch));
			} else {
				client.putZoneOnTop(this);
			}
		} else {
			doTouchDown(touch);
		}
	}
	
	protected void touchUpImpl(Touch touch) {
		if (drawer.isOpen() && !isInDrawer && isAboveTrash)
			client.remove(this);
	}
	
	private MoveableItem copyAndAssignTouch(MoveableItem oldItem, Touch touch) {
		MoveableItem copy = new MoveableItem(x, y, width, height, getGlobalMatrix());
		copy.drawer = drawer;
		copy.container = container;
		copy.primaryColour = primaryColour;
		copy.secondaryColour = secondaryColour;
		
		copy.assign(touch);
		oldItem.unassign(touch);
		
		// apply additional actions
		copyImpl(oldItem, copy);
		
		return copy;
	}
	
	protected void copyImpl(MoveableItem oldItem, MoveableItem newItem) {
		newItem.name = "copy of " + oldItem.name;
	}
	
	protected void doTouchDown(Touch touch) {
		System.out.println("Touched: " + name);
	}
	
	public void putIntoDrawer(RotatingDrawer drawer) {
		isInDrawer = true;
		this.drawer = drawer;
	}
	
	public void setContainer(RotatingContainer container) {
		this.container = container;
	}
}
