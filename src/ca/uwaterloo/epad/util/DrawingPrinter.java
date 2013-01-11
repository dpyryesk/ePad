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

import processing.core.PImage;

public class DrawingPrinter implements Runnable {
	private PImage drawing;
	private boolean showPrompt;

	private void printDrawing() {
		PrinterJob pjob = PrinterJob.getPrinterJob();
		Book book = new Book();

		PageFormat landscape = pjob.defaultPage();
		landscape.setOrientation(PageFormat.LANDSCAPE);
		book.append(new DrawingPage(drawing), landscape);

		pjob.setPageable(book);
		boolean ok = pjob.printDialog();
		if (ok) {
			try {
				pjob.print();
			} catch (PrinterException e) {
				e.printStackTrace();
			}
		}
	}

	private void printDrawingWithoutDialog() {
		PrinterJob pjob = PrinterJob.getPrinterJob();
		Book book = new Book();

		PageFormat landscape = pjob.defaultPage();
		landscape.setOrientation(PageFormat.LANDSCAPE);
		book.append(new DrawingPage(drawing), landscape);

		pjob.setPageable(book);
		try {
			pjob.print();
		} catch (PrinterException e) {
			e.printStackTrace();
		}
	}
	
	public DrawingPrinter(PImage drawing, boolean showPrompt) {
		this.drawing = drawing;
		this.showPrompt = showPrompt;
	}

	public void run() {
		if (showPrompt)
			printDrawing();
		else
			printDrawingWithoutDialog();
	}
	
	private static class DrawingPage implements Printable {
		private PImage drawing;
		
		public DrawingPage(PImage drawing) {
			this.drawing = drawing;
		}

		@Override
		public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
			if (page > 0) {
				return NO_SUCH_PAGE;
			}

			// Graphics2D g2d = (Graphics2D) g;
			// g2d.translate(pf.getImageableX(), pf.getImageableY());
			g.translate(0, 0);
			int formh = (int) pf.getHeight();
			int formw = (int) pf.getWidth();

			g.drawImage((Image) drawing.getNative(), 0, 0, formw, formh, null);

			return PAGE_EXISTS;
		}
		
	}
}
