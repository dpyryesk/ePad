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

import org.apache.log4j.Logger;

import processing.core.PGraphics;
import processing.core.PShape;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.xml.XmlAttribute;

/**
 * This class represents a Stamp widget that paints a certain shape on the
 * screen. It has a single parameter <b>stampFile</b> which represents the path
 * to the shape file (a vector image with the extension <i>svg</i>).
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see Brush
 * @see MoveableItem
 * 
 */
public class Stamp extends Brush {
	private static final Logger LOGGER = Logger.getLogger(Stamp.class);

	/**
	 * The path to the shape file.</br>This parameter can be retrieved
	 * automatically from XML files using
	 * {@link ca.uwaterloo.epad.xml.SimpleMarshaller SimpleMarshaller} class.
	 */
	@XmlAttribute
	public String stampFile;

	// Shape of the stamp
	private PShape stampShape;
	// Should the style of the shape be disabled?
	private boolean disableStyle = true;
	// Shape dimensions
	private float stampWidth, stampHeight;

	/**
	 * Default constructor that allows creating Stamp objects manually.
	 * 
	 * @param path
	 *            path to the shape file.
	 */
	public Stamp(String path) {
		stampFile = path;
		loadShape();
	}

	/**
	 * Constructor that builds a copy of another Stamp object
	 * 
	 * @param original
	 *            the original Stamp object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public Stamp(Stamp original) {
		super(original);
		stampFile = original.stampFile;
		disableStyle = original.disableStyle;
		name = original.name;
		stampShape = original.stampShape;
		stampWidth = original.stampWidth;
		stampHeight = original.stampHeight;
		loadShape();
	}

	/**
	 * Constructor that builds a copy of another MoveableItem object
	 * 
	 * @param original
	 *            the original MoveableItem object.
	 * @see MoveableItem#MoveableItem(MoveableItem)
	 */
	public Stamp(MoveableItem original) {
		super(original);
	}

	/**
	 * Load and resize the specified shape.
	 */
	private void loadShape() {
		if (stampShape == null && stampFile != null) {
			stampShape = applet.loadShape(Settings.dataFolder + stampFile);
			if (stampShape == null)
				LOGGER.error("Failed to load shape: " + stampFile);
			else {
				stampWidth = stampShape.getWidth();
				stampHeight = stampShape.getHeight();

				// Scale the shape while preserving proportions
				if (stampWidth > stampHeight) {
					stampHeight = 100 * stampHeight / stampWidth;
					stampWidth = 100;
				} else {
					stampWidth = 100 * stampWidth / stampHeight;
					stampHeight = 100;
				}

				if (disableStyle)
					stampShape.disableStyle();
			}
		}
	}

	/**
	 * Load and resize the specified shape in the initialisation step.
	 */
	public void doInit() {
		loadShape();
	}

	/**
	 * Pencil renders strokes by drawing the specified shape in the initial
	 * point of the stroke. Other points are ignored, so that only a single
	 * shape is drawn per stroke.
	 */
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0)
			return;
		if (length == 1) {
			// Try loading the shape in case it is missing
			loadShape();
			StrokePoint p = s.getPath().get(0);

			if (stampShape != null) {
				g.beginDraw();
				g.beginShape();
				g.shapeMode(CENTER);
				if (disableStyle) {
					g.fill(colour);
					g.noStroke();
					g.strokeWeight(1);
				}
				g.shape(stampShape, p.x, p.y, stampWidth, stampHeight);
				g.endShape();
				g.endDraw();
			}
		}
	}
}
