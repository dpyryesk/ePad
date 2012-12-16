package ca.uwaterloo.epad.painting;

import processing.core.PGraphics;
import processing.core.PShape;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.xml.XmlAttribute;

public class Stamp extends Brush {
	@XmlAttribute public String stampFile;
	
	private PShape stampShape;
	private boolean badFile = false;
	private boolean disableStyle = true;
	private float stampWidth, stampHeight;
	
	public Stamp(Stamp original) {
		super(original);
		stampFile = original.stampFile;
		disableStyle = original.disableStyle;
		name = original.name;
		stampShape = original.stampShape;
		stampWidth = original.stampWidth;
		stampHeight = original.stampHeight;
	}
	
	public Stamp(MoveableItem original) {
		super(original);
	}
	
	protected void drawItem() {
		if (badFile) return;
		
		if (stampShape == null) {
			try {
				stampShape = applet.loadShape(stampFile);
				stampWidth = stampShape.getWidth();
				stampHeight = stampShape.getHeight();
				
				// scale while preserving proportions
				if (stampWidth > stampHeight) {
					stampHeight = 100 * stampHeight / stampWidth;
					stampWidth = 100;
				} else {
					stampWidth = 100 * stampWidth / stampHeight;
					stampHeight = 100;
				}
			} catch (Exception e) {
				System.out.println("Unable to load stamp: " + stampFile + ". Error: " + e.getMessage());
				badFile = true;
				return;
			}
			if (disableStyle)
				stampShape.disableStyle();
		}
		beginShape();
		shapeMode(CENTER);
		if (disableStyle) {
			fill(0);
			stroke(0);
			strokeWeight(1);
		}
		shape(stampShape, width / 2, height / 2, stampWidth, stampHeight);
		endShape();
	}
	
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0) return;
		if (length == 1) {
			StrokePoint p = s.getPath().get(0);
			
			g.beginDraw();
			g.beginShape();
			g.shapeMode(CENTER);
			if (disableStyle) {
				g.fill(colour);
				g.stroke(colour);
				g.strokeWeight(1);
			}
			g.shape(stampShape, p.x, p.y, stampWidth, stampHeight);
			g.endShape();
			g.endDraw();
		}
	}
}
