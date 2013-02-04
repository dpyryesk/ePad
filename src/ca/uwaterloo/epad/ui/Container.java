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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.uwaterloo.epad.Application;

import vialab.SMT.Zone;

/**
 * This abstract class implements the basic functionality shared by all
 * container widgets. Container widgets are used to organise child widgets.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public abstract class Container extends Zone {
	private static final Logger LOGGER = Logger.getLogger(Container.class);

	/**
	 * The event ID for "container was moved" event.
	 */
	public static final String MOVED = "moved";

	/**
	 * Default width of an item.
	 */
	public static final int ITEM_WIDTH = 125;

	/**
	 * Default height of an item.
	 */
	public static final int ITEM_HEIGHT = 125;

	// Colours
	protected int primaryColour = Application.primaryColour;
	protected int secondaryColour = Application.secondaryColour;
	protected int transparentColour = Application.transparentColour;
	protected int transparentAlpha = Application.transparentAlpha;

	// Map of child zones
	protected Map<Integer, Zone> items;
	// Number of child zones
	protected int itemCount = 0;
	// Parent drawer
	protected Drawer parent;
	// Array of event listeners
	protected ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	// Default constructor
	protected Container(int x, int y, int width, int height, Drawer parent) {
		super(x, y, width, height);
		this.parent = parent;
		items = new HashMap<Integer, Zone>();
	}

	/**
	 * Add a child item to the container.
	 * 
	 * @param item
	 *            a zone object to be added to the container
	 * @return <b>true</b> if the object was successfully added to the container
	 *         and <b>false</b> otherwise
	 */
	abstract public boolean addItem(Zone item);

	/**
	 * Retrieve an item contained in the container based on its ID (the ID
	 * depends on the order in which items were added to the container i.e.
	 * 0,1,2,3..).
	 * 
	 * @param id
	 *            ID of the item
	 * @return child item if its ID exists in the list and <b>null</b> otherwise
	 */
	public Zone getItemByID(int id) {
		return items.get(id);
	}

	// Draw the container
	@Override
	abstract protected void drawImpl();

	// Draw for zone picker
	@Override
	abstract protected void pickDrawImpl();

	// Action on the touch event
	@Override
	abstract protected void touchImpl();

	/**
	 * Set the colour scheme of the container.
	 * 
	 * @param primary
	 *            primary colour
	 * @param secondary
	 *            secondary colour
	 */
	public void setColourScheme(int primary, int secondary) {
		primaryColour = primary;
		secondaryColour = secondary;
	}

	public int getPrimaryColour() {
		return primaryColour;
	}

	public int getSecondaryColour() {
		return secondaryColour;
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
