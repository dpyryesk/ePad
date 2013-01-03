package ca.uwaterloo.epad.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import processing.core.PVector;

import ca.uwaterloo.epad.Application;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;

public abstract class Drawer extends Zone {
	public static final String OPEN = "open";
	public static final String CLOSED = "closed";
	
	protected boolean isOpen;
	protected int position;

	protected int dragXMin, dragXMax, dragYMin, dragYMax;
	protected boolean dragX, dragY;
	protected Container container;

	protected long lastActionTime;
	protected boolean wasTouched = false;

	protected ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	protected Drawer(int x, int y, int width, int height, int position) {
		super(x, y, width, height);

		isOpen = false;
		this.position = position;

		setActionPerformed();
	}

	abstract protected void drawImpl();

	abstract protected void pickDrawImpl();

	protected void touchDownImpl(Touch touch) {
		TouchClient.putZoneOnTop(this);
	}

	protected void touchImpl() {
		drag(dragX, dragY, dragXMin, dragXMax, dragYMin, dragYMax);

		// figure out if the drawer is opened
		boolean newState = isOpen();
		if (newState != isOpen) {
			isOpen = isOpen();
			notifyListeners();
		}
		Application.setActionPerformed();
		setActionPerformed();
	}

	abstract public boolean isOpen();

	abstract public boolean isItemAbove(Zone item);

	abstract public PVector getHandleLocation();

	public int getPosition() {
		return position;
	}

	public Container getContainer() {
		return container;
	}

	public void setColourScheme(int primary, int secondary) {
		container.setColourScheme(primary, secondary);
	}

	public void setColourScheme(int primary, int secondary, int background) {
		container.setColourScheme(primary, secondary, background);
	}

	public int getPrimaryColour() {
		return container.getPrimaryColour();
	}

	public int getSecondaryColour() {
		return container.getSecondaryColour();
	}

	public int getBackgroundColour() {
		return container.getBackgroundColour();
	}

	public void setActionPerformed() {
		lastActionTime = new Date().getTime();
	}

	public long getInactiveTime() {
		return new Date().getTime() - lastActionTime;
	}

	protected void notifyListeners() {
		String s;
		if (isOpen) s = OPEN;
		else s = CLOSED;
		
		for (ActionListener name : listeners) {
			name.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, s));
		}
	}

	public void addListener(ActionListener listener) {
		listeners.add(listener);
	}
	
	public boolean removeListener(ActionListener listener) {
		return listeners.remove(listener);
	}
}
