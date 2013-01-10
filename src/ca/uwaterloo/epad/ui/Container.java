package ca.uwaterloo.epad.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import vialab.SMT.Zone;

public abstract class Container extends Zone {
	public static final String MOVED = "moved";
	
	public static final int ITEM_WIDTH = 125;
	public static final int ITEM_HEIGHT = 125;
	
	protected int primaryColour = 255;
	protected int secondaryColour = 1;
	protected int backgroundColour = 0x33000000;
	
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
	
	public void setColourScheme(int primary, int secondary, int background) {
		primaryColour = primary;
		secondaryColour = secondary;
		backgroundColour = background;
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
	
	protected void notifyListeners(String message) {
		for (int i = 0; i < listeners.size(); i++) {
			ActionListener listener = listeners.get(i);
			if (listener != null)
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, message));
			else
				System.err.println("Container.notifyListeners(): null list element " + i);
		}
	}

	public void addListener(ActionListener listener) {
		listeners.add(listener);
	}
	
	public boolean removeListener(ActionListener listener) {
		return listeners.remove(listener);
	}
}
