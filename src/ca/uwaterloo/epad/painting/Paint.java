package ca.uwaterloo.epad.painting;

import processing.core.PImage;
import vialab.SMT.Touch;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.xml.XmlAttribute;

public class Paint extends MoveableItem {
	@XmlAttribute public int paintColour;
	
	protected static PImage paintImage;
	//protected static PShape paintShape;

	public Paint(int colour) {
		super(0, 0, 150, 150);
		this.paintColour = colour;
		isSelected = false;
		name = ""+colour;
		
		if (paintImage == null)
			paintImage = applet.loadImage("data\\images\\paintCan.png");
		
		//if (paintShape == null)
		//	paintShape = applet.loadShape("data\\vector\\paint_can.svg");
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
		
		//if (paintShape == null)
		//	paintShape = applet.loadShape("data\\vector\\paint_can.svg");
	}
	
	protected void drawItem() {
		imageMode(CENTER);
		tint(paintColour);
		image(paintImage, width/2, height/2, 100, 100);
		
		/*
		beginShape();
		shapeMode(CORNER);
		shape(paintShape, 12.5f, 12.5f);
		
		PShape child = paintShape.getChild("path25");
		child.disableStyle();
		child.fill(paintColour);
		child.noStroke();
		fill(paintColour);
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("path35");
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("path41");
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("path43");
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("path45");
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("path47");
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("path49");
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("path39");
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("path37");
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("ellipse27");
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("ellipse29");
		shape(child, 12.5f, 12.5f);
		
		child = paintShape.getChild("ellipse31");
		shape(child, 12.5f, 12.5f);
		
		endShape();
		*/
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
