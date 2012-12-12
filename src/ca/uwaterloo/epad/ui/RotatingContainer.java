package ca.uwaterloo.epad.ui;

import java.util.HashMap;
import java.util.Map;

import vialab.SMT.Zone;

public class RotatingContainer extends Zone  {
	// Layout parameters
	private static int itemWidth = 125;		//width of each item
	private static int itemHeight = 125;	//height of each item
	private static int offsetAngle = 15;	//angular offset between items in degrees
	private static int offsetDistance = 15;	//distance between rows of items
	private static int itemCountMax = 2 * 360 / offsetAngle;	//maximum number of items container may hold
	
	private int primaryColour = 255;
	private int secondaryColour = 1;
	private int backgroundColour = 0x33000000;
	
	private int diameter;
	private Map<Integer, MoveableItem> items;
	private int itemCount = 0;
	
	public RotatingContainer (RotatingDrawer parent) {
		super(0, 0, parent.getDiameter(), parent.getDiameter());
		diameter = parent.getDiameter() - 50;
		
		items = new HashMap<Integer, MoveableItem>(itemCountMax/2);
	}
	
	public boolean addItem(MoveableItem item) {
		if (itemCount >= itemCountMax)
			return false;
		
		item.width = itemWidth;
		item.height = itemHeight;
		item.setColourScheme(primaryColour, secondaryColour);
		item.setContainer(this);
		
		float angle = itemCount * offsetAngle * DEG_TO_RAD;
		item.rotateAbout(angle, width/2, height/2);
		if (itemCount < 360 / offsetAngle)
			item.translate(width/2 - itemWidth/2, height/2 + diameter/2 - (itemHeight + offsetDistance));
		else
			item.translate(width/2 - itemWidth/2, height/2 + diameter/2 - 2 * (itemHeight + offsetDistance));
		
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