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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import processing.core.PMatrix3D;
import processing.core.PVector;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;

/**
 * This abstract class implements the basic functionality shared by all drawer
 * widgets. Drawers are initially located at the edges of the screen so that
 * only a handle is visible, then they can be opened by dragging the handle.
 * Drawers are used by this application to organise items such as brushes and
 * paints. Drawers have an inner Container that handles and displays child
 * items.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public abstract class Drawer extends Zone {
	private static final Logger LOGGER = Logger.getLogger(Drawer.class);

	/**
	 * The event ID for "drawer was opened" event.
	 */
	public static final String OPEN = "open";
	/**
	 * The event ID for "drawer was closed" event.
	 */
	public static final String CLOSED = "closed";
	/**
	 * The event ID for "drawer was moved" event.
	 */
	public static final String MOVED = "moved";

	// Drawer opened flag
	protected boolean isOpen;
	// Drawer position variable, it can be LEFT, RIGHT, TOP or BOTTOM
	protected int position;
	// Is the drawer being touched
	protected boolean isTouched = false;

	/**
	 * The visible width at which the drawer is considered open.
	 */
	public int openWidth = 100;
	/**
	 * Auto close flag to indicate whether or not the drawer should close
	 * automatically.
	 */
	public boolean autoClose = true;

	// Colours
	protected int primaryColour = Application.primaryColour;
	protected int secondaryColour = Application.secondaryColour;

	// Drag limiters
	protected int dragXMin, dragXMax, dragYMin, dragYMax;
	// Drag flags that enable dragging along x and/or y axis
	protected boolean dragX, dragY;
	// Container that manages child items
	protected Container container;
	// Array of event listeners
	protected ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	// Default constructor
	protected Drawer(int x, int y, int width, int height, int position) {
		super(x, y, width, height);

		isOpen = false;
		this.position = position;
	}

	// Draw the drawer
	@Override
	abstract protected void drawImpl();

	// Draw for zone picker
	@Override
	abstract protected void pickDrawImpl();

	// Action on the touch down event
	@Override
	protected void touchDownImpl(Touch touch) {
		TouchClient.putZoneOnTop(this);
		isTouched = true;
	}

	// Action on the touch up event
	@Override
	protected void touchUpImpl(Touch touch) {
		isTouched = false;
	}

	// Action on the touch event
	@Override
	protected void touchImpl() {
		drag(dragX, dragY, dragXMin, dragXMax, dragYMin, dragYMax);

		// figure out if the drawer is opened
		boolean newState = calculateOpenedState();
		if (isOpen != newState) {
			isOpen = newState;
			if (isOpen)
				notifyListeners(OPEN);
			else
				notifyListeners(CLOSED);
		}
		Application.setActionPerformed();
	}

	protected void slide(float dx, float dy) {
		if (dx == 0 && dy == 0) {
			return;
		}

		pushMatrix();
		setMatrix(new PMatrix3D());

		if (dragX && dx != 0) {
			translate(dx, 0);
		}

		if (dragY && dy != 0) {
			translate(0, dy);
		}

		PVector prevPos = fromZoneVector(new PVector(0, 0));

		int maxLeftMove = -(int) (prevPos.x - dragXMin);
		int maxRightMove = (int) (dragXMax - (prevPos.x + width));
		int maxUpMove = -(int) (prevPos.y - dragYMin);
		int maxDownMove = (int) (dragYMax - (prevPos.y + height));

		// respect the limits by translating back to limit if needed
		if (dx < maxLeftMove) {
			translate(-(dx - maxLeftMove), 0);
		}
		if (dx > maxRightMove) {
			translate(-(dx - maxRightMove), 0);
		}
		if (dy < maxUpMove) {
			translate(0, -(dy - maxUpMove));
		}
		if (dy > maxDownMove) {
			translate(0, -(dy - maxDownMove));
		}

		matrix.preApply(new PMatrix3D(getMatrix()));
		popMatrix();
	}

	/**
	 * Is drawer currently opened?
	 * 
	 * @return <b>true</b> if the drawer is opened, <b>false</b> otherwise
	 */
	public boolean isOpen() {
		return isOpen;
	}

	// Calculate whether or not the drawer is opened
	abstract protected boolean calculateOpenedState();

	/**
	 * Calculate whether or not the given item is above the drawer.
	 * 
	 * @param item
	 *            a zone object to be checked
	 * @return <b>true</b> if the item is above the drawer and <b>false</b>
	 *         otherwise
	 */
	abstract public boolean isItemAbove(Zone item);

	/**
	 * Get the location of the drawer's handle in global coordinates.
	 * 
	 * @return the location vector
	 */
	abstract public PVector getHandleLocation();

	/**
	 * Get the width of the visible portion of the drawer (how much of the
	 * drawer was dragged out to the screen).
	 * 
	 * @return width of the visible portion in pixels
	 */
	abstract public float getVisibleWidth();

	/**
	 * 
	 * @return position of the drawer (processing.core.PConstants.LEFT,
	 *         processing.core.PConstants.RIGHT, processing.core.PConstants.TOP
	 *         or processing.core.PConstants.BOTTOM)
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * 
	 * @return the internal container
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * Set the colour scheme of the container.
	 * 
	 * @param primary
	 *            primary colour
	 * @param secondary
	 *            secondary colour
	 */
	public void setColourScheme(int primary, int secondary) {
		container.setColourScheme(primary, secondary);
	}

	public int getPrimaryColour() {
		return container.getPrimaryColour();
	}

	public int getSecondaryColour() {
		return container.getSecondaryColour();
	}

	// Send a message to all listeners
	protected void notifyListeners(String message) {
		for (int i = 0; i < listeners.size(); i++) {
			ActionListener listener = listeners.get(i);
			if (listener != null)
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, message));
			else
				LOGGER.error("A listener is null: " + i);
		}
	}

	/**
	 * Add a listener.
	 * 
	 * @param listener
	 *            listener object to add
	 */
	public void addListener(ActionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Remove a listener.
	 * 
	 * @param listener
	 *            listener object to remove
	 * @return <b>true</b> if the list of listeners contained the specified
	 *         object
	 */
	public boolean removeListener(ActionListener listener) {
		return listeners.remove(listener);
	}
}
