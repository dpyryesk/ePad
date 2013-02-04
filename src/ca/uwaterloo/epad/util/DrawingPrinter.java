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

package ca.uwaterloo.epad.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import org.apache.log4j.Logger;

import processing.core.PImage;

/**
 * This class implements the image printing functionality.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class DrawingPrinter implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(DrawingPrinter.class);

	// Image to print
	private PImage drawing;
	// Whether or not a printing dialog should be displayed before printing
	private boolean showPrompt;

	// Attempt to print the drawing with a print dialog
	private void printDrawing() {
		// Create a new printer job and a book to print
		PrinterJob pjob = PrinterJob.getPrinterJob();
		Book book = new Book();

		// Set orientation to landscape
		PageFormat landscape = pjob.defaultPage();
		landscape.setOrientation(PageFormat.LANDSCAPE);

		// Add the drawing to the book
		book.append(new DrawingPage(drawing), landscape);

		// Add the book to the print job
		pjob.setPageable(book);

		// Show the dialog and print if the user presses "OK"
		boolean ok = pjob.printDialog();
		if (ok) {
			try {
				pjob.print();
			} catch (PrinterException e) {
				LOGGER.error("Printing error: " + e.getLocalizedMessage());
			}
		}
	}

	// Attempt to print the drawing without a print dialog
	private void printDrawingWithoutDialog() {
		// Create a new printer job and a book to print
		PrinterJob pjob = PrinterJob.getPrinterJob();
		Book book = new Book();

		// Set orientation to landscape
		PageFormat landscape = pjob.defaultPage();
		landscape.setOrientation(PageFormat.LANDSCAPE);

		// Add the drawing to the book
		book.append(new DrawingPage(drawing), landscape);

		// Add the book to the print job
		pjob.setPageable(book);

		// Print without the dialog. The system's default printer will be used
		try {
			pjob.print();
		} catch (PrinterException e) {
			LOGGER.error("Printing error: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Default constructor.
	 * 
	 * @param drawing
	 *            image to print
	 * @param showPrompt
	 *            <b>true</b> if a printing dialog should be displayed and
	 *            <b>false</b> otherwise
	 */
	public DrawingPrinter(PImage drawing, boolean showPrompt) {
		this.drawing = drawing;
		this.showPrompt = showPrompt;
	}

	@Override
	public void run() {
		LOGGER.info("Drawing printer started.");

		if (showPrompt)
			printDrawing();
		else
			printDrawingWithoutDialog();
	}

	/**
	 * Private class to represent the printing page with the drawing.
	 * 
	 * @author Dmitry Pyryeskin
	 * @version 1.0
	 * 
	 */
	private static class DrawingPage implements Printable {
		// Native image to print
		private Image image;

		/**
		 * Default constructor.
		 * 
		 * @param drawing
		 *            image to print
		 */
		public DrawingPage(PImage drawing) {
			drawing.parent = null;
			image = (Image) drawing.getNative();
		}

		@Override
		public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
			if (page > 0) {
				return NO_SUCH_PAGE;
			}

			g.translate(0, 0);
			
			// Get the dimensions of the printable area of the page
			int x = (int) pf.getImageableX();
			int y = (int) pf.getImageableY();
			int h = (int) pf.getImageableHeight();
			int w = (int) pf.getImageableWidth();

			// Resize the image while preserving its aspect
			double imageAspect = (double) image.getWidth(null) / image.getHeight(null);
			double pageAspect = pf.getImageableWidth() / pf.getImageableHeight();

			if (Math.abs(imageAspect - pageAspect) > 0.01) {
				if (pageAspect > imageAspect) {
					w = (int) (h * imageAspect);
				} else {
					h = (int) (w / imageAspect);
				}
				LOGGER.info("The aspect ratio of the printed image is preserved, value=" + imageAspect);
			}

			// Add the image to the page
			g.drawImage(image, x, y, w, h, null);

			return PAGE_EXISTS;
		}
	}
}
