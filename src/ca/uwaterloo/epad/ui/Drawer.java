package ca.uwaterloo.epad.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import processing.core.PVector;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;

public abstract class Drawer extends Zone {
	public static final String OPEN = "open";
	public static final String CLOSED = "closed";
	public static final String MOVED = "moved";
	
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
			if (isOpen) notifyListeners(OPEN);
			else notifyListeners(CLOSED);
		}
		Application.setActionPerformed();
		setActionPerformed();
	}

	abstract public boolean isOpen();

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
		lastActionTime = System.currentTimeMillis();
	}

	public long getInactiveTime() {
		return System.currentTimeMillis() - lastActionTime;
	}

	protected void notifyListeners(String message) {
		for (int i = 0; i < listeners.size(); i++) {
			ActionListener listener = listeners.get(i);
			if (listener != null)
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, message));
			else
				System.err.println("Drawer.notifyListeners(): null list element " + i);
		}
	}

	public void addListener(ActionListener listener) {
		listeners.add(listener);
	}
	
	public boolean removeListener(ActionListener listener) {
		return listeners.remove(listener);
	}
}
