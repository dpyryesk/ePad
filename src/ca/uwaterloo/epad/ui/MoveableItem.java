/*
 *	ePad 2.0 Multitouch Customizable Painting Platform
 *  Copyright (C) 2012 Dmitry Pyryeskin and Jesse Hoey, University of Waterloo
 *  
 *  This file is part of ePad 2.0.
 *
 *  ePad 2.0 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ePad 2.0 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with ePad 2.0. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.uwaterloo.epad.ui;

import processing.core.PImage;
import processing.core.PShape;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;

public class MoveableItem extends Zone {
	// Position parameters
	protected boolean isInDrawer = false;
	protected boolean isAboveTrash = false;
	protected boolean isSelected = false;
	protected boolean isDrawerOpen = false;
	protected boolean isDragged = false;
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
			moveIcon = applet.loadShape(Settings.dataFolder + "vector\\move.svg");
		if (deleteIcon == null) {
			deleteIcon = applet.loadShape(Settings.dataFolder + "vector\\x.svg");
			deleteIcon.disableStyle();
		}
	}
	
	public MoveableItem (MoveableItem original) {
		this(original.x, original.y, original.width, original.height);
		matrix = original.getGlobalMatrix();
		drawerId = original.drawerId;
		highlightColour = original.highlightColour;
		backgroundColour = original.backgroundColour;
		deleteColour = original.deleteColour;
		name = original.name;
		itemImage = original.itemImage;
		itemImageFilename = original.itemImageFilename;
	}
	
	protected void drawImpl() {
		isDrawerOpen = Application.getDrawer(Application.LEFT_DRAWER).isOpen() || Application.getDrawer(Application.RIGHT_DRAWER).isOpen();
		
		pushMatrix();
		
		// Set stroke colour
		if (isDrawerOpen) {
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
		if (!isInDrawer && isDrawerOpen) {
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
		if (!isInDrawer && isDrawerOpen) {
			ellipse(0, 0, 30, 30);
		}
	}
	
	protected void touchImpl() {
		if (!isInDrawer && isDrawerOpen) {
			rst();
			isDragged = true;
			//check if item is above a trash can
			isAboveTrash = Application.isItemAboveDrawer(this);
		}
		
		Application.setActionPerformed();
		
		if (isInDrawer)
			drawer.setActionPerformed();
	}
	
	protected void touchDownImpl(Touch touch) {
		if (isDrawerOpen) {
			if (isInDrawer) {
				MoveableItem copy = clone(this.getClass());
				if (copy != null) {
					copy.assign(touch);
					this.unassign(touch);
					copy.setDrawer(drawerId, false);
					Application.addItem(copy);
				}
			} else {
				TouchClient.putZoneOnTop(this);
			}
		} else {
			doTouchDown(touch);
		}
	}
	
	protected void touchUpImpl(Touch touch) {
		if (isDrawerOpen && !isInDrawer && isAboveTrash) {
			Application.removeItem(this);
			doTouchUp(touch);
		}
		isDragged = false;
	}
	
	private MoveableItem clone(Object enclosingClass) {
		MoveableItem clone;
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
		
		drawer = Application.getDrawer(drawerId);
		container = drawer.getContainer();
		setColourScheme(container.getPrimaryColour(), container.getSecondaryColour());
		
		if (isInDrawer) {
			boolean success = container.addItem(this);
			if (!success)
				System.err.println("Failed to add an item to container");
		} else {
			isDrawerOpen = drawer.isOpen();
		}
	}
	
	public void addToScreen() {
		Application.addItem(this);
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
			itemImage = applet.loadImage(Settings.dataFolder + filename);
		} catch (Exception e) {
			e.printStackTrace();
			itemImage = null;
		}
	}
	
	public int getDrawerId() {
		return drawerId;
	}
	
	public boolean getIsDragged() {
		return isDragged;
	}
}
