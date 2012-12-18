package ca.uwaterloo.epad.ui;

import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;

public abstract class Drawer extends Zone {
	protected boolean isOpen;
	protected int position;
	
	protected int dragXMin, dragXMax, dragYMin, dragYMax;
	protected boolean dragX, dragY;
	protected Container container;
	
	protected Drawer(int x, int y, int width, int height, int position) {
		super(x, y, width, height);
		
		isOpen = false;
		this.position = position;
	}
	
	abstract protected void drawImpl();
	
	abstract protected void pickDrawImpl();
	
	protected void touchDownImpl(Touch touch) {
		TouchClient.putZoneOnTop(this);
	}
	
	protected void touchImpl() {
		drag(dragX, dragY, dragXMin, dragXMax, dragYMin, dragYMax);
		
		// figure out if the drawer is opened
		isOpen = isOpen();
	}
	
	abstract public boolean isOpen();
	
	abstract public boolean isItemAbove(MoveableItem item);
	
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
}
