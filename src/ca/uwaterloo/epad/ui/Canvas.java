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

package ca.uwaterloo.epad.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import processing.core.PGraphics;
import processing.core.PImage;
import vialab.SMT.Touch;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.painting.SpiderBrush;
import ca.uwaterloo.epad.painting.Stroke;

public class Canvas extends Zone {
	private static final Logger LOGGER = Logger.getLogger(Canvas.class);
	
	public int backgroundColour = 255;
	public int borderColour = Application.primaryColour;
	public int transparentColour = Application.transparentColour;
	public int transparentAlpha = Application.transparentAlpha;

	private HashMap<Long, Stroke> strokes;
	private PGraphics drawing;
	private String overlayImagePath;
	private PImage overlayImage;
	public boolean useOverlay = false;
	private boolean isDrawerOpen;

	public Canvas(int x, int y, int width, int height, int backgroundColour) {
		super(0, 0, width, height);
		translate(x, y);
		isDrawerOpen = false;
		this.backgroundColour = backgroundColour;
		drawing = applet.createGraphics(width, height, P2D);
		clear();
	}

	protected void drawImpl() {
		isDrawerOpen = Application.getDrawer(Application.LEFT_DRAWER).isOpen() || Application.getDrawer(Application.RIGHT_DRAWER).isOpen();
		
		if (isDrawerOpen) {
			// Draw border to indicate the moving state
			stroke(borderColour);
			strokeWeight(2);
			fill(transparentColour, transparentAlpha);
			rect(-30, -30, width + 60, height + 60, 30);
		}
		
		noStroke();
		fill(backgroundColour);
		rect(0, 0, width, height);

		image(drawing, 0, 0, width, height);
		
		if (useOverlay && overlayImage != null)
			image(overlayImage, 0, 0, width, height);
	}

	protected void pickDrawImpl() {
		if (isDrawerOpen)
			rect(-30, -30, width + 60, height + 60, 30);
		else
			rect(0, 0, width, height);
	}

	protected void touchImpl() {
		if (isDrawerOpen)
			rst();
		else
			addStroke();
		
		Application.setActionPerformed();
	}

	protected void addStroke() {
		if (!getTouchMap().isEmpty()) {
			Set<Long> keys = getTouchMap().keySet();
			Iterator<Long> it = keys.iterator();

			while (it.hasNext()) {
				Long id = it.next();
				Touch t = getTouchMap().get(id);

				if (strokes.containsKey(id)) {
					// update stroke
					Stroke s = strokes.get(id);
					s.update(t);
					s.render(drawing);
				} else {
					// new stroke
					Stroke s = new Stroke(t, this);
					strokes.put(id, s);
					s.render(drawing);
				}
			}
		}
	}
	
	public PImage getDrawing(boolean hideOverlay) {
		boolean doToggleOverlay = useOverlay && hideOverlay;
		if (doToggleOverlay)
			toggleOverlay();
		
		PGraphics result = applet.createGraphics(width, height, P2D);
		result.beginDraw();
		result.image(drawing, 0, 0, width, height);
		if (useOverlay && overlayImage != null)
			result.blend(overlayImage, 0, 0, width, height, 0, 0, width, height, DARKEST);
		result.endDraw();
		
		if (doToggleOverlay)
			toggleOverlay();
		
		return result.get();
	}
	
	public void clear() {
		drawing.beginDraw();
		drawing.noStroke();
		drawing.fill(backgroundColour);
		drawing.rect(0, 0, width, height);
		drawing.endDraw();
		
		if (strokes == null)
			strokes = new HashMap<Long, Stroke>();
		else
			strokes.clear();
		
		// special clear for spider brush
		SpiderBrush.clearStrokes();
	}
	
	public void clearAndLoad(String filename) {
		clear();
		
		PImage underlayImage = applet.loadImage(filename);
		if (underlayImage == null) {
			LOGGER.error("Failed to load image: " + filename);
		} else {
			drawing.beginDraw();
			drawing.image(underlayImage, 0, 0, width, height);
			drawing.endDraw();
		}
	}
	
	public String getOverlayImagePath() {
		if (useOverlay)
			return overlayImagePath;
		else
			return null;
	}
	
	public void setOverlayImage(String filename) {
		overlayImagePath = filename;
		overlayImage = applet.loadImage(filename);
		if (overlayImage == null)
			LOGGER.error("Failed to load image: " + filename);
			
		useOverlay = true;
	}
	
	public void toggleOverlay() {
		useOverlay = !useOverlay;
	}
}
