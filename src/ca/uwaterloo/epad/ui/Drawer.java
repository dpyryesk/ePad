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

import processing.core.PVector;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;

public abstract class Drawer extends Zone {
	private static final Logger LOGGER = Logger.getLogger(Drawer.class);
	
	public static final String OPEN = "open";
	public static final String CLOSED = "closed";
	public static final String MOVED = "moved";
	
	protected boolean isOpen;
	protected int position;
	
	protected int primaryColour = Application.primaryColour;
	protected int secondaryColour = Application.secondaryColour;

	protected int dragXMin, dragXMax, dragYMin, dragYMax;
	protected boolean dragX, dragY;
	protected Container container;

	protected boolean wasTouched = false;

	protected ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	protected Drawer(int x, int y, int width, int height, int position) {
		super(x, y, width, height);

		isOpen = false;
		this.position = position;
	}

	@Override
	abstract protected void drawImpl();

	@Override
	abstract protected void pickDrawImpl();

	@Override
	protected void touchDownImpl(Touch touch) {
		TouchClient.putZoneOnTop(this);
	}

	@Override
	protected void touchImpl() {
		drag(dragX, dragY, dragXMin, dragXMax, dragYMin, dragYMax);

		// figure out if the drawer is opened
		boolean newState = calculateState();
		if (isOpen != newState) {
			isOpen = newState;
			if (isOpen) notifyListeners(OPEN);
			else notifyListeners(CLOSED);
		}
		Application.setActionPerformed();
	}
	
	public boolean isOpen() {
		return isOpen;
	}

	abstract protected boolean calculateState();
	
	abstract public boolean isItemAbove(Zone item);

	abstract public PVector getHandleLocation();
	
	abstract public float getVisibleWidth();

	public int getPosition() {
		return position;
	}

	public Container getContainer() {
		return container;
	}

	public void setColourScheme(int primary, int secondary) {
		container.setColourScheme(primary, secondary);
	}

	public int getPrimaryColour() {
		return container.getPrimaryColour();
	}

	public int getSecondaryColour() {
		return container.getSecondaryColour();
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
