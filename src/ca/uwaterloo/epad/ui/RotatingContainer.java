package ca.uwaterloo.epad.ui;

import java.util.HashMap;
import java.util.Map;

import vialab.SMT.Zone;

public class RotatingContainer extends Zone  {
	// Layout parameters
	public static final int ITEM_WIDTH = 125;		//width of each item
	public static final int ITEM_HEIGHT = 125;	//height of each item
	public static final int OFFSET_ANGLE = 15;	//angular offset between items in degrees
	public static final int OFFSET_DIST = 15;	//distance between rows of items
	public static final int ITEM_COUNT_MAX = 2 * 360 / OFFSET_ANGLE;	//maximum number of items container may hold
	
	private int primaryColour = 255;
	private int secondaryColour = 1;
	private int backgroundColour = 0x33000000;
	
	private int diameter;
	private Map<Integer, MoveableItem> items;
	private int itemCount = 0;
	
	public RotatingContainer (RotatingDrawer parent) {
		super(0, 0, parent.getDiameter(), parent.getDiameter());
		diameter = parent.getDiameter() - 50;
		
		items = new HashMap<Integer, MoveableItem>(ITEM_COUNT_MAX/2);
	}
	
	public boolean addItem(MoveableItem item) {
		if (itemCount >= ITEM_COUNT_MAX)
			return false;
		
		item.width = ITEM_WIDTH;
		item.height = ITEM_HEIGHT;
		
		float angle = itemCount * OFFSET_ANGLE * DEG_TO_RAD;
		item.rotateAbout(angle, width/2, height/2);
		if (itemCount < 360 / OFFSET_ANGLE)
			item.translate(width/2 - ITEM_WIDTH/2, height/2 + diameter/2 - (ITEM_HEIGHT + OFFSET_DIST));
		else
			item.translate(width/2 - ITEM_WIDTH/2, height/2 + diameter/2 - 2 * (ITEM_HEIGHT + OFFSET_DIST));
		
		add(item);
		
		items.put(new Integer(itemCount), item);
		
		itemCount++;
		
		return true;
	}
	
	protected void drawImpl() {
		pushMatrix();
		translate(width/2, height/2);
		
		noStroke();
		
		ellipseMode(CENTER);
		fill(backgroundColour);
		ellipse(0, 0, diameter, diameter);
		fill(primaryColour);
		ellipse(0, 0, diameter/2+100, diameter/2+100);
		
		/*
		stroke(secondaryColour);
		strokeWeight(3);
		line (-diameter/2, 0, diameter/2, 0);
		line (0, -diameter/2, 0, diameter/2);
		*/
		
		popMatrix();
	}
	
	protected void pickDrawImpl() {
		ellipseMode(CENTER);
		ellipse(width/2, height/2, diameter, diameter);
	}
	
	protected void touchImpl() {
		rotateAboutCentre();
	}
	
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