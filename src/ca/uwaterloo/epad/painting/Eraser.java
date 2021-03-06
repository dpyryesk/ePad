/*
 *	ePad 2.0 Multitouch Customizable Painting Platform
 *  Copyright (C) 2012 Dmitry Pyryeskin and Jesse Hoey, University of Waterloo
 *  
 *  This file is part of ePad 2.0.
 *
 *  ePad 2.0 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ePad 2.0 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with ePad 2.0. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.uwaterloo.epad.painting;

import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.xml.XmlAttribute;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * 
 * This class represents a circular eraser. It has a single parameter
 * <b>size</b> which represents the diameter of the eraser.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see Brush
 * @see MoveableItem
 */
public class Eraser extends Brush {
	/**
	 * The diameter of the circular eraser.</br>This parameter can be retrieved
	 * automatically from XML files using
	 * {@link ca.uwaterloo.epad.xml.SimpleMarshaller SimpleMarshaller} class.
	 */
	@XmlAttribute
	public int size;

	/**
	 * Default constructor that allows creating Eraser objects manually.
	 * 
	 * @param size
	 *            diameter of the eraser.
	 */
	public Eraser(int size) {
		super();
		this.size = size;
	}

	/**
	 * Constructor that builds a copy of another Eraser object.
	 * 
	 * @param original
	 *            the original Eraser object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public Eraser(Eraser original) {
		super(original);
		size = original.size;
	}

	/**
	 * Constructor that builds a copy of another MoveableItem object.
	 * 
	 * @param original
	 *            the original MoveableItem object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public Eraser(MoveableItem original) {
		super(original);
	}

	/**
	 * Eraser renders strokes by drawing thick lines with stroke weight equal to
	 * <b>size</b> parameter and colour equal to the background colour of the
	 * canvas.
	 * 
	 * @see ca.uwaterloo.epad.ui.Canvas
	 */
	@Override
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0)
			return;
		if (length == 1) {
			StrokePoint p = s.getPath().get(length - 1);

			g.beginDraw();
			g.noStroke();
			g.fill(Application.getCanvas().backgroundColour);
			g.ellipseMode(PConstants.CENTER);
			g.ellipse(p.x, p.y, size, size);
			g.endDraw();
		} else {
			StrokePoint from = s.getPath().get(length - 2);
			StrokePoint to = s.getPath().get(length - 1);

			g.beginDraw();
			g.strokeJoin(PConstants.ROUND);
			g.strokeCap(PConstants.ROUND);
			g.stroke(Application.getCanvas().backgroundColour);
			g.strokeWeight(size);
			g.line(from.x, from.y, to.x, to.y);
			g.endDraw();
		}
	}

}
