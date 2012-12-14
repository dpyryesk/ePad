package ca.uwaterloo.epad.painting;

import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.xml.XmlAttribute;
import processing.core.PConstants;
import processing.core.PGraphics;

public class Eraser extends Brush {
	@XmlAttribute public int size;

	public Eraser(int size, int canvasColour) {
		super();
		this.size = size;
		name = "Eraser " + size;
	}
	
	public Eraser(Eraser original) {
		super(original);
		size = original.size;
		name = original.name;
	}
	
	public Eraser(MoveableItem original) {
		super(original);
	}
	
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0) return;
		if (length == 1) {
			StrokePoint p = s.getPath().get(length-1);
			
			g.beginDraw();
			g.noStroke();
			g.fill(Application.canvas.backgroundColour);
			g.ellipseMode(PConstants.CENTER);
			g.ellipse(p.x, p.y, size, size);
			g.endDraw();
		} else {
			StrokePoint from = s.getPath().get(length-2);
			StrokePoint to = s.getPath().get(length-1);
			
			g.beginDraw();
			g.strokeJoin(PConstants.ROUND);
			g.strokeCap(PConstants.ROUND);
			g.stroke(Application.canvas.backgroundColour);
			g.strokeWeight(size);
			g.line(from.x, from.y, to.x, to.y);
			g.endDraw();
		}
	}

}
