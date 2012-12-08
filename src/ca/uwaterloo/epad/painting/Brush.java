package ca.uwaterloo.epad.painting;

import processing.core.PGraphics;
import vialab.SMT.Touch;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;

public abstract class Brush extends MoveableItem {
	protected int brushWidth, brushHeight;
	
	public Brush(int width, int height) {
		super(0, 0, 150, 150);
		brushWidth = width;
		brushHeight = height;
		isSelected = false;
		name = ""+width+"x"+height;
	}
	
	public Brush(Brush original) {
		super(original);
		brushWidth = original.brushWidth;
		brushHeight = original.brushHeight;
		isSelected = false;
	}
	
	protected void doTouchDown(Touch touch) {
		Application.setBrush(this);
	}
	
	protected void doTouchUp(Touch touch) {
		Application.setBrush(null);
	}
	
	protected abstract void drawItem();
	
	public abstract void renderStroke(Stroke s, int colour, PGraphics g);
}
