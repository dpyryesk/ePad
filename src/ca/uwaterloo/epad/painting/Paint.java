package ca.uwaterloo.epad.painting;

import processing.core.PImage;
import vialab.SMT.Touch;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.xml.XmlAttribute;

public class Paint extends MoveableItem {
	@XmlAttribute public int paintColour;
	
	protected static PImage paintImage;

	public Paint(int colour) {
		super(0, 0, 150, 150);
		this.paintColour = colour;
		isSelected = false;
		name = ""+colour;
		
		if (paintImage == null)
			paintImage = applet.loadImage("data\\images\\paintCan.png");
	}
	
	public Paint(Paint original) {
		super(original);
		paintColour = original.paintColour;
		isSelected = false;
	}
	
	public Paint(MoveableItem original) {
		super(original);
		
		if (paintImage == null)
			paintImage = applet.loadImage("..\\data\\images\\paintCan.png");
	}
	
	protected void drawItem() {
		imageMode(CENTER);
		tint(paintColour);
		image(paintImage, width/2, height/2, 100, 100);
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
