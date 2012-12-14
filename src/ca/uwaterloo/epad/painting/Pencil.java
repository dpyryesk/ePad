package ca.uwaterloo.epad.painting;

import processing.core.PConstants;
import processing.core.PGraphics;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.xml.XmlAttribute;

public class Pencil extends Brush {
	@XmlAttribute public int size;

	public Pencil(int size) {
		super();
		this.size = size;
		name = "Pencil " + size;
	}
	
	public Pencil(Pencil original) {
		super(original);
		size = original.size;
		name = "Pencil " + size;
	}
	
	public Pencil(MoveableItem original) {
		super(original);
		name = "Pencil " + size;
	}
	
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0) return;
		if (length == 1) {
			StrokePoint p = s.getPath().get(length-1);
			
			g.beginDraw();
			g.noStroke();
			g.fill(colour);
			g.ellipseMode(PConstants.CENTER);
			g.ellipse(p.x, p.y, size, size);
			g.endDraw();
		} else {
			StrokePoint from = s.getPath().get(length-2);
			StrokePoint to = s.getPath().get(length-1);
			
			g.beginDraw();
			g.strokeJoin(PConstants.ROUND);
			g.strokeCap(PConstants.ROUND);
			g.stroke(colour);
			g.strokeWeight(size);
			g.line(from.x, from.y, to.x, to.y);
			g.endDraw();
		}
	}

}
