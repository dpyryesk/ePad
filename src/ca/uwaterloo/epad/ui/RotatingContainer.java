package ca.uwaterloo.epad.ui;

import ca.uwaterloo.epad.Application;
import vialab.SMT.Zone;


public class RotatingContainer extends Container  {
	// Layout parameters
	public static final int OFFSET_ANGLE = 15;	//angular offset between items in degrees
	public static final int OFFSET_DIST = 15;	//distance between rows of items
	public static final int ITEM_COUNT_MAX = 2 * 360 / OFFSET_ANGLE;	//maximum number of items container may hold
	
	private int diameter;
	
	public RotatingContainer (int diameter, Drawer parent) {
		super(0, 0, diameter, diameter, parent);
		this.diameter = diameter - 50;
	}
	
	public boolean addItem(Zone item) {
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
		Application.setActionPerformed();
		parent.setActionPerformed();
		notifyListeners(MOVED);
	}
}