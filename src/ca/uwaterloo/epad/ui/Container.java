package ca.uwaterloo.epad.ui;

import java.util.HashMap;
import java.util.Map;

import vialab.SMT.Zone;

public abstract class Container extends Zone {
	public static final int ITEM_WIDTH = 125;	//width of each item
	public static final int ITEM_HEIGHT = 125;	//height of each item

	protected int primaryColour = 255;
	protected int secondaryColour = 1;
	protected int backgroundColour = 0x33000000;
	
	protected Map<Integer, Zone> items;
	protected int itemCount = 0;
	protected Drawer parent;
	
	public Container(int x, int y, int width, int height, Drawer parent) {
		super(x, y, width, height);
		this.parent = parent;
		items = new HashMap<Integer, Zone>();
	}
	
	abstract public boolean addItem(Zone item);
	
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
}
