package ca.uwaterloo.epad.ui;

import processing.core.PImage;
import processing.core.PShape;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;

public class MoveableItem extends Zone {
	// Position parameters
	protected boolean isInDrawer = false;
	protected boolean isAboveTrash = false;
	protected boolean isSelected;
	protected int drawerId;
	
	// Parent containers
	protected Container container;
	protected Drawer drawer;
	
	// Item's image
	protected PImage itemImage;
	protected String itemImageFilename;
	
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
		drawerId = original.drawerId;
		primaryColour = original.primaryColour;
		secondaryColour = original.secondaryColour;
		highlightColour = original.highlightColour;
		backgroundColour = original.backgroundColour;
		deleteColour = original.deleteColour;
		name = original.name;
		itemImage = original.itemImage;
		itemImageFilename = original.itemImageFilename;
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
			isAboveTrash = Application.isItemAboveDrawer(this);
		}
	}
	
	protected void touchDownImpl(Touch touch) {
		if (drawer.isOpen()) {
			if (isInDrawer) {
				Zone copy = clone(this.getClass());
				if (copy != null) {
					copy.assign(touch);
					this.unassign(touch);
					TouchClient.add(copy);
				}
			} else {
				TouchClient.putZoneOnTop(this);
			}
		} else {
			doTouchDown(touch);
		}
	}
	
	protected void touchUpImpl(Touch touch) {
		if (drawer.isOpen() && !isInDrawer && isAboveTrash) {
			TouchClient.remove(this);
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
	
	/**
	 * Performs an initialisation step
	 */
	public void doInit() {
	}
	
	public void setDrawer(int drawerId, boolean isInDrawer) {
		this.isInDrawer = isInDrawer;
		this.drawerId = drawerId;
		
		switch(drawerId) {
			case Application.TOP_DRAWER: drawer = Application.topDrawer; break;
			case Application.BOTTOM_DRAWER: drawer = Application.bottomDrawer; break;
			case Application.LEFT_DRAWER: drawer = Application.leftDrawer; break;
			case Application.RIGHT_DRAWER: drawer = Application.rightDrawer; break;
		}
		
		container = drawer.getContainer();
		setColourScheme(container.getPrimaryColour(), container.getSecondaryColour());
		
		if (isInDrawer) {
			boolean success = container.addItem(this);
			if (!success) System.err.println("Failed to add an item to container");
		}
	}
	
	public void addToScreen() {
		TouchClient.add(this);
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
	
	public String getImage() {
		return itemImageFilename;
	}
	
	public void setImage(String filename) {
		try {
			itemImageFilename = filename;
			itemImage = applet.loadImage(filename);
		} catch (Exception e) {
			e.printStackTrace();
			itemImage = null;
		}
	}
	
	public int getDrawerId() {
		return drawerId;
	}
}
