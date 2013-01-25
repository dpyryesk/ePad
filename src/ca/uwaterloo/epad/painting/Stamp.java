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

public class Stamp extends Brush {
	private static final Logger LOGGER = Logger.getLogger(Stamp.class);
	
	@XmlAttribute public String stampFile;
	
	private PShape stampShape;
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
		loadShape();
	}
	
	public Stamp(MoveableItem original) {
		super(original);
	}
	
	private void loadShape() {
		if (stampShape == null && stampFile != null) {
			stampShape = applet.loadShape(Settings.dataFolder + stampFile);
			if (stampShape == null)
				LOGGER.error("Failed to load shape: " + stampFile);
			else {
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
				
				if (disableStyle)
					stampShape.disableStyle();
			}
		}
	}
	
	public void doInit() {
		loadShape();
	}
	
	public void renderStroke(Stroke s, int colour, PGraphics g) {
		int length = s.getPath().size();
		if (length == 0) return;
		if (length == 1) {
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
