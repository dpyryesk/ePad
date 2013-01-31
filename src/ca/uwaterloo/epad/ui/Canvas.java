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

/**
 * This class is the implementation of Canvas widget that is provides the
 * painting functionality. It switches into movement mode when the side drawers
 * are open, but when the drawers are closed, it captures the location of
 * touches and renders strokes accordingly to the selected brush and paint. The
 * Canvas also supports a colouring mode, which is similar to the colouring
 * books.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class Canvas extends Zone {
	private static final Logger LOGGER = Logger.getLogger(Canvas.class);

	// Colours
	public int backgroundColour = 255;
	public int borderColour = Application.primaryColour;
	public int transparentColour = Application.transparentColour;
	public int transparentAlpha = Application.transparentAlpha;

	// Map of stroke objects
	private HashMap<Long, Stroke> strokes;
	// Graphics object that renders the drawing
	private PGraphics drawing;
	// Path to the overlay image (for colouring mode)
	private String overlayImagePath;
	// Overlay image object
	private PImage overlayImage;
	// Colouring mode flag
	public boolean useOverlay = false;
	// Drawers open flag
	private boolean isDrawerOpen;

	/**
	 * Default constructor.
	 * 
	 * @param x
	 *            x-coordinate of the top left corner of the canvas
	 * @param y
	 *            w-coordinate of the top left corner of the canvas
	 * @param width
	 *            width of the canvas
	 * @param height
	 *            height of the canvas
	 * @param backgroundColour
	 *            colour of the canvas
	 */
	public Canvas(int x, int y, int width, int height, int backgroundColour) {
		super(0, 0, width, height);
		translate(x, y);
		isDrawerOpen = false;
		this.backgroundColour = backgroundColour;
		drawing = applet.createGraphics(width, height, P2D);
		clear();
	}

	// Draw the canvas
	@Override
	protected void drawImpl() {
		isDrawerOpen = Application.getDrawer(Application.LEFT_DRAWER).isOpen() || Application.getDrawer(Application.RIGHT_DRAWER).isOpen();

		if (isDrawerOpen) {
			// Draw a border to indicate the moving state
			stroke(borderColour);
			strokeWeight(2);
			fill(transparentColour, transparentAlpha);
			rect(-30, -30, width + 60, height + 60, 30);
		}

		// Draw background
		noStroke();
		fill(backgroundColour);
		rect(0, 0, width, height);

		// Draw the main image
		image(drawing, 0, 0, width, height);

		// Draw overlay image
		if (useOverlay && overlayImage != null)
			image(overlayImage, 0, 0, width, height);
	}

	// Draw for zone picker
	@Override
	protected void pickDrawImpl() {
		if (isDrawerOpen)
			rect(-30, -30, width + 60, height + 60, 30);
		else
			rect(0, 0, width, height);
	}

	// Action on touch event
	@Override
	protected void touchImpl() {
		if (isDrawerOpen)
			rst();
		else
			addStroke();

		Application.setActionPerformed();
	}

	// Add active touches to the stroke map
	protected void addStroke() {
		if (!getTouchMap().isEmpty()) {
			// Get all active touches associated with Canvas
			Set<Long> keys = getTouchMap().keySet();
			Iterator<Long> it = keys.iterator();

			while (it.hasNext()) {
				Long id = it.next();
				Touch t = getTouchMap().get(id);

				if (strokes.containsKey(id)) {
					// Update stroke if it already existed in the map
					Stroke s = strokes.get(id);
					s.update(t);
					s.render(drawing);
				} else {
					// Add new stroke
					Stroke s = new Stroke(t, this);
					strokes.put(id, s);
					s.render(drawing);
				}
			}
		}
	}

	/**
	 * Return the current drawing.
	 * 
	 * @param hideOverlay
	 *            if <b>false</b> then the overlay image will be blended over
	 *            the main image (in an overlay image is defined) and if
	 *            <b>true</b> then the overlay image will be ignored.
	 * @return the current drawing with or without the overlay image (depending
	 *         on the parameter)
	 */
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

	/**
	 * Clear the current drawing and erase all strokes.
	 */
	public void clear() {
		// Clear the main drawing
		drawing.beginDraw();
		drawing.background(backgroundColour);
		drawing.noStroke();
		drawing.fill(backgroundColour);
		drawing.rect(0, 0, width, height);
		drawing.endDraw();

		// Clear strokes
		if (strokes == null)
			strokes = new HashMap<Long, Stroke>();
		else
			strokes.clear();

		// Special clear for the spider brush
		SpiderBrush.clearStrokes();
	}

	/**
	 * Clear the current drawing and load the specified image file.
	 * 
	 * @param filename
	 *            path to the image file to load into the drawing
	 */
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

	/**
	 * Get the path to the current overlay image.
	 * 
	 * @return the path to the current overlay image
	 */
	public String getOverlayImagePath() {
		if (useOverlay)
			return overlayImagePath;
		else
			return null;
	}

	/**
	 * Set the overlay image and enable the colouring mode. The areas of the
	 * specified file that are meant to be coloured in must be transparent.
	 * 
	 * @param filename
	 *            path to the image file to be used as overlay
	 */
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
