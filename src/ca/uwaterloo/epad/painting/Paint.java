package ca.uwaterloo.epad.painting;

import vialab.SMT.Touch;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;

public class Paint extends MoveableItem {
	protected int paintColour;

	public Paint(int colour) {
		super(0, 0, 150, 150);
		this.paintColour = colour;
		isSelected = false;
		name = ""+colour;
	}
	
	public Paint(Paint original) {
		super(original);
		paintColour = original.paintColour;
		isSelected = false;
	}
	
	protected void drawItem() {
		imageMode(CENTER);
		tint(paintColour);
		image(Application.paintCan, width/2, height/2, 100, 100);
	}
	
	protected void doTouchDown(Touch touch) {
		Application.setPaint(this);
	}
	
	protected void doTouchUp(Touch touch) {
		Application.setPaint(null);
	}
	
	public int getColour() {
		return paintColour;
	}
}
