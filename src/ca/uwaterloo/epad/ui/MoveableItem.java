package ca.uwaterloo.epad.ui;

import processing.core.PVector;
import vialab.SMT.Touch;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;

public class MoveableItem extends Zone {
	protected boolean isInDrawer = false;
	protected boolean isAboveTrash = false;
	protected boolean isSelected;
	
	public int primaryColour = 255;
	public int secondaryColour = 0;
	public int deleteColour = 0xFFCC0000;
	protected RotatingContainer container;
	protected RotatingDrawer drawer;
	
	public MoveableItem (int x, int y, int width, int height) {
		this("", x, y, width, height);
	}
	
	public MoveableItem (String name, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.name = name;
	}
	
	public MoveableItem (MoveableItem original) {
		this(original.x, original.y, original.width, original.height);
		matrix = original.getGlobalMatrix();
		drawer = original.drawer;
		container = original.container;
		primaryColour = original.primaryColour;
		secondaryColour = original.secondaryColour;
		name = "copy of " + original.name;
	}
	
	protected void drawImpl() {
		pushMatrix();
		
		// Set stroke colour
		if (drawer.isOpen()) {
			if (isAboveTrash)
				stroke(deleteColour);
			else
				stroke(primaryColour);
			strokeWeight(2);
		} else {
			noStroke();
		}
		
		// Set background colour
		if (isSelected)
			fill(color(red(secondaryColour), green(secondaryColour), blue(secondaryColour), 200));
		else
			fill(0x22000000);
		ellipseMode(CENTER);
		ellipse(width/2, height/2, width, height);
		
		// Draw the item
		pushStyle();
		pushMatrix();
		drawItem();
		popMatrix();
		popStyle();
		
		// Draw the icon
		if (!isInDrawer && drawer.isOpen()) {
			noStroke();
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
	
	protected void drawItem() {
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
				Zone copy = clone(this.getClass());
				if (copy != null) {
					copy.assign(touch);
					this.unassign(touch);
					client.add(copy);
				}
			} else {
				client.putZoneOnTop(this);
			}
		} else {
			doTouchDown(touch);
		}
	}
	
	protected void touchUpImpl(Touch touch) {
		if (drawer.isOpen() && !isInDrawer && isAboveTrash) {
			client.remove(this);
			doTouchUp(touch);
		}
	}
	
	private Zone clone(Object enclosingClass) {
		Zone clone;
		try {
			// if inner class, call its constructor properly by passing its
			// enclosing class too
			if (this.getClass().getEnclosingClass() != null
					&& this.getClass().getEnclosingClass() == enclosingClass.getClass()) {
				// clone a Zone using a copy constructor
				clone = this.getClass()
						.getConstructor(this.getClass().getEnclosingClass(), this.getClass())
						.newInstance(enclosingClass, this);
			}
			else {
				// clone a Zone using a copy constructor
				clone = this.getClass().getConstructor(this.getClass()).newInstance(this);
			}
		}
		catch (Exception e) {
			clone = null;
			System.err.println("Unable to clone zone " + enclosingClass.getClass().getCanonicalName());
		}
		
		return clone;
	}
	
	protected void doTouchDown(Touch touch) {
	}
	
	protected void doTouchUp(Touch touch) {
	}
	
	public void putIntoDrawer(RotatingDrawer drawer) {
		isInDrawer = true;
		this.drawer = drawer;
	}
	
	public void setDrawer(RotatingDrawer drawer) {
		this.drawer = drawer;
	}
	
	public void setContainer(RotatingContainer container) {
		this.container = container;
	}
	
	public void select() {
		isSelected = true;
	}
	
	public void deselect() {
		isSelected = false;
	}
	
	public boolean isSelected() {
		return isSelected;
	}
}
