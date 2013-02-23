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

import org.apache.log4j.Logger;

import processing.core.PImage;
import processing.core.PShape;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.xml.SimpleMarshaller;

/**
 * This class implements the default functionality of all widgets used in ePad
 * application, such as the default drawing method, ability to be placed into
 * and dragged out of the drawers and default multitouch interactions (move,
 * scale and rotate).</br><b>Important:</b> every new widget must extend this
 * class to take the advantage of this default functionality and also to be
 * handled properly by {@link SimpleMarshaller} class.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class MoveableItem extends Zone {
	private static final Logger LOGGER = Logger.getLogger(MoveableItem.class);

	// Position parameters
	protected boolean isInDrawer = false;
	protected boolean isAboveDrawer = false;
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

	// Shared graphics
	protected static PShape moveIcon, deleteIcon;

	// Colour scheme
	protected int primaryColour = Application.primaryColour;
	protected int secondaryColour = Application.secondaryColour;
	protected int highlightColour = Application.primaryColour;
	protected int transparentColour = Application.transparentColour;
	protected int transparentAlpha = Application.transparentAlpha;
	protected int deleteColour = Application.deleteColour;

	/**
	 * Default constructor.
	 * 
	 * @param x
	 *            x-coordinate of the top right corner of the item
	 * @param y
	 *            y-coordinate of the top right corner of the item
	 * @param width
	 *            width of the item
	 * @param height
	 *            height of the item
	 */
	public MoveableItem(int x, int y, int width, int height) {
		super(x, y, width, height);

		// Load icon shapes
		if (moveIcon == null) {
			moveIcon = applet.loadShape(Settings.dataFolder + "vector\\move.svg");
			if (moveIcon == null)
				LOGGER.error("Failed to load shape: " + Settings.dataFolder + "vector\\move.svg");
		}
		if (deleteIcon == null) {
			deleteIcon = applet.loadShape(Settings.dataFolder + "vector\\x.svg");
			if (moveIcon == null)
				LOGGER.error("Failed to load shape: " + Settings.dataFolder + "vector\\x.svg");
			else
				deleteIcon.disableStyle();
		}
	}

	/**
	 * Constructor that builds a copy of another MoveableItem object. It is
	 * important that the subclasses of MoveableItem class override this
	 * constructor since it is used in {@link SimpleMarshaller} class.
	 * 
	 * @param original
	 *            the original MoveableItem object.
	 */
	public MoveableItem(MoveableItem original) {
		this(original.x, original.y, original.width, original.height);
		matrix = original.getGlobalMatrix();
		drawerId = original.drawerId;
		highlightColour = original.highlightColour;
		transparentColour = original.transparentColour;
		transparentAlpha = original.transparentAlpha;
		deleteColour = original.deleteColour;
		name = original.name;
		itemImage = original.itemImage;
		itemImageFilename = original.itemImageFilename;
	}

	// Draw the item
	@Override
	protected void drawImpl() {
		// Check if any of the drawers is open
		isDrawerOpen = Application.getDrawer(Application.LEFT_DRAWER).isOpen() || Application.getDrawer(Application.RIGHT_DRAWER).isOpen();

		pushMatrix();

		// Set stroke colour
		if (isDrawerOpen) {
			if (isAboveDrawer)
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
			fill(transparentColour, transparentAlpha);
		ellipseMode(CENTER);
		ellipse(width / 2, height / 2, width, height);

		// Draw the item
		pushStyle();
		pushMatrix();
		drawItem();
		popMatrix();
		popStyle();

		// Draw the icons if necessary
		if (!isInDrawer && isDrawerOpen) {
			noStroke();
			fill(primaryColour);
			ellipse(0, 0, 30, 30);

			if (isAboveDrawer) {
				// Draw the delete icon when the item is above a drawer
				if (deleteIcon != null) {
					fill(deleteColour);
					shape(deleteIcon, -15, -15, 30, 30);
				}
			} else {
				// Draw the move icon when any of the drawers is open
				if (moveIcon != null)
					shape(moveIcon, -15, -15, 30, 30);
			}
		}

		popMatrix();
	}

	// Draw for zone picker
	@Override
	protected void pickDrawImpl() {
		ellipseMode(CENTER);
		ellipse(width / 2, height / 2, width, height);
		if (!isInDrawer && isDrawerOpen) {
			ellipse(0, 0, 30, 30);
		}
	}

	// Action on the touch event
	@Override
	protected void touchImpl() {
		if (!isInDrawer && isDrawerOpen) {
			// Rotate, scale and translate based on the touches
			rst();
			isDragged = true;
			// Check if item is above a any of the drawers
			isAboveDrawer = Application.isItemAboveDrawer(this);
		}

		Application.setActionPerformed();
	}

	// Action on the touch down event
	@Override
	protected void touchDownImpl(Touch touch) {
		if (isDrawerOpen) {
			if (isInDrawer) {
				// Create a copy of the item in a drawer and assign the touch to
				// the copy
				MoveableItem copy = clone(this.getClass());
				if (copy != null) {
					copy.assign(touch);
					this.unassign(touch);
					copy.setDrawer(drawerId, false);
					Application.addItem(copy);
				}
			} else {
				// Put the item on top
				TouchClient.putZoneOnTop(this);
			}
		} else {
			// Process the touch in a subclass
			doTouchDown(touch);
		}
	}

	// Action on the touch up event
	@Override
	protected void touchUpImpl(Touch touch) {
		if (isDrawerOpen) {
			isDragged = false;
			if (!isInDrawer && isAboveDrawer) {
				// Remove the item if it is above one of the drawers
				Application.removeItem(this);
			}
		} else {
			// Process the touch in a subclass
			doTouchUp(touch);
		}
	}

	// Clone the item
	private MoveableItem clone(Object enclosingClass) {
		MoveableItem clone;
		try {
			// If inner class, call its constructor properly by passing its
			// enclosing class too
			if (this.getClass().getEnclosingClass() != null && this.getClass().getEnclosingClass() == enclosingClass.getClass()) {
				// clone a Zone using a copy constructor
				clone = this.getClass().getConstructor(this.getClass().getEnclosingClass(), this.getClass()).newInstance(enclosingClass, this);
			} else {
				// Clone a Zone using a copy constructor
				clone = this.getClass().getConstructor(this.getClass()).newInstance(this);
			}
		} catch (Exception e) {
			clone = null;
			LOGGER.error("Failed to clone zone " + enclosingClass.getClass().getCanonicalName());
		}

		return clone;
	}

	/**
	 * Override this function in a subclass to perform an initialisation step.
	 */
	public void doInit() {
	}

	/**
	 * Override this function in a subclass to draw custom graphics on top of
	 * the item.
	 */
	protected void drawItem() {
	}

	/**
	 * Override this function in a subclass to invoke some functionality on
	 * <i>touch down</i> event.
	 * 
	 * @param touch
	 *            Touch object that triggered the event
	 */
	protected void doTouchDown(Touch touch) {
	}

	/**
	 * Override this function in a subclass to invoke some functionality on
	 * <i>touch up</i> event.
	 * 
	 * @param touch
	 *            Touch object that triggered the event
	 */
	protected void doTouchUp(Touch touch) {
	}

	/**
	 * Set the item's parent drawer.
	 * 
	 * @param drawerId
	 *            must be one of the following: {@link Application#TOP_DRAWER},
	 *            {@link Application#LEFT_DRAWER} or
	 *            {@link Application#RIGHT_DRAWER}; the item will be added to
	 *            the appropriate drawer, if it exists in the GUI.
	 * @param isInDrawer
	 *            set to <b>true</b> if you would like to put the item into the
	 *            drawer and to <b>false</b> if the item will be added directly
	 *            to the screen.
	 */
	public void setDrawer(int drawerId, boolean isInDrawer) {
		this.isInDrawer = isInDrawer;
		this.drawerId = drawerId;

		drawer = Application.getDrawer(drawerId);
		if (drawer == null)
			return;

		container = drawer.getContainer();
		setColourScheme(container.getPrimaryColour(), container.getSecondaryColour());

		if (isInDrawer) {
			boolean success = container.addItem(this);
			if (!success)
				LOGGER.error("Failed to add an item to container");
		} else {
			isDrawerOpen = drawer.isOpen();
		}
	}

	/**
	 * Add the item to the screen.
	 */
	public void addToScreen() {
		Application.addItem(this);
	}

	/**
	 * Mark the item as selected (it will be drawn with a highlight).
	 * 
	 * @see MoveableItem#deselect()
	 */
	public void select() {
		isSelected = true;
	}

	/**
	 * Mark the item as not selected.
	 * 
	 * @see MoveableItem#select()
	 */
	public void deselect() {
		isSelected = false;
	}

	/**
	 * 
	 * @return <b>true</b> if the item is marked as selected and <b>false</b>
	 *         otherwise
	 * 
	 * @see MoveableItem#select()
	 * @see MoveableItem#deselect()
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Set the colour scheme of the item.
	 * 
	 * @param primary
	 *            primary colour
	 * @param secondary
	 *            secondary colour
	 */
	public void setColourScheme(int primary, int secondary) {
		primaryColour = primary;
		secondaryColour = secondary;
		highlightColour = secondaryColour - 0x33000000;
	}

	public int getPrimaryColour() {
		return primaryColour;
	}

	public int getSecondaryColour() {
		return secondaryColour;
	}

	/**
	 * Set the image of the item to the specified file.
	 * 
	 * @param filename
	 *            a valid path to an image file
	 */
	public void setImage(String filename) {
		itemImageFilename = filename;
		itemImage = applet.loadImage(Settings.dataFolder + filename);
		if (itemImage == null)
			LOGGER.error("Failed to load image: " + Settings.dataFolder + filename);
	}

	/**
	 * 
	 * @return the path to the item's image.
	 */
	public String getImageFilename() {
		return itemImageFilename;
	}

	/**
	 * 
	 * @return ID of the item's parent drawer, it must be one of the following:
	 *         {@link Application#TOP_DRAWER}, {@link Application#LEFT_DRAWER}
	 *         or {@link Application#RIGHT_DRAWER}
	 */
	public int getDrawerId() {
		return drawerId;
	}

	/**
	 * 
	 * @return <b>true</b> if the item is currently being moved around and
	 *         <b>false</b> otherwise
	 */
	public boolean getIsDragged() {
		return isDragged;
	}
}
