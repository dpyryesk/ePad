package ca.uwaterloo.epad.ui;

import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import vialab.SMT.Touch;
import vialab.SMT.Zone;

public class MoveableItem extends Zone {
	// Position parameters
	protected boolean isInDrawer = false;
	protected boolean isAboveTrash = false;
	protected boolean isSelected;
	
	// Parent containers
	protected RotatingContainer container;
	protected RotatingDrawer drawer;
	
	// Item's image
	protected PImage itemImage;
	
	// Common graphics
	protected static PShape moveIcon, deleteIcon;
	
	// Colour scheme
	protected int primaryColour = 255;
	protected int secondaryColour = 0;
	protected int highlightColour = 128;
	protected int backgroundColour = 0x33000000;
	protected int deleteColour = 0xFFCC0000;
	
	public MoveableItem (int x, int y, int width, int height) {
		this("", x, y, width, height);
	}
	
	public MoveableItem (String name, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.name = name;
		
		// Load shapes
		if (moveIcon == null)
			moveIcon = applet.loadShape("..\\data\\vector\\move.svg");
		if (deleteIcon == null) {
			deleteIcon = applet.loadShape("..\\data\\vector\\x.svg");
			deleteIcon.disableStyle();
		}
	}
	
	public MoveableItem (MoveableItem original) {
		this(original.x, original.y, original.width, original.height);
		matrix = original.getGlobalMatrix();
		drawer = original.drawer;
		container = original.container;
		primaryColour = original.primaryColour;
		secondaryColour = original.secondaryColour;
		highlightColour = original.highlightColour;
		backgroundColour = original.backgroundColour;
		deleteColour = original.deleteColour;
		name = original.name;
		itemImage = original.itemImage;
	}
	
	public void setImage(String filename) {
		try {
			itemImage = applet.loadImage(filename);
		} catch (Exception e) {
			e.printStackTrace();
			itemImage = null;
		}
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
			//fill(color(red(secondaryColour), green(secondaryColour), blue(secondaryColour), 0xCC));
			fill(highlightColour);
		else
			fill(backgroundColour);
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
				fill(deleteColour);
				shape(deleteIcon, -15, -15, 30, 30);
			} else {
				//draw move icon
				shape(moveIcon, -15, -15, 30, 30);
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
	
	public void setColourScheme(int primary, int secondary) {
		primaryColour = primary;
		secondaryColour = secondary;
		highlightColour = secondaryColour - 0x33000000;
	}
	
	public void setColourScheme(int primary, int secondary, int background) {
		primaryColour = primary;
		secondaryColour = secondary;
		highlightColour = secondaryColour - 0x33000000;
		backgroundColour = background;
	}
	
	public void setColourScheme(int primary, int secondary, int background, int delete) {
		primaryColour = primary;
		secondaryColour = secondary;
		highlightColour = secondaryColour - 0x33000000;
		backgroundColour = background;
		deleteColour = delete;
	}
	
	public int getPrimaryColour() {
		return primaryColour;
	}
	
	public int getSecondaryColour() {
		return secondaryColour;
	}
	
	public int getBackgroundColour() {
		return backgroundColour;
	}
}
