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

public abstract class Container extends Zone {
	private static final Logger LOGGER = Logger.getLogger(Container.class);
	
	public static final String MOVED = "moved";
	
	public static final int ITEM_WIDTH = 125;
	public static final int ITEM_HEIGHT = 125;
	
	protected int primaryColour = Application.primaryColour;
	protected int secondaryColour = Application.secondaryColour;
	protected int transparentColour = Application.transparentColour;
	protected int transparentAlpha = Application.transparentAlpha;
	
	protected Map<Integer, Zone> items;
	protected int itemCount = 0;
	protected Drawer parent;
	
	protected ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	
	public Container(int x, int y, int width, int height, Drawer parent) {
		super(x, y, width, height);
		this.parent = parent;
		items = new HashMap<Integer, Zone>();
	}
	
	abstract public boolean addItem(Zone item);
	
	public Zone getItemByID(int id) {
		return items.get(id);
	}
	
	abstract protected void drawImpl();
	
	abstract protected void pickDrawImpl();
	
	abstract protected void touchImpl();
	
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
	
	protected void notifyListeners(String message) {
		for (int i = 0; i < listeners.size(); i++) {
			ActionListener listener = listeners.get(i);
			if (listener != null)
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, message));
			else
				LOGGER.error("A listener is null: " + i);
		}
	}

	public void addListener(ActionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public boolean removeListener(ActionListener listener) {
		return listeners.remove(listener);
	}
}
