package ca.uwaterloo.epad;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import processing.core.PImage;

public class DrawingPrinter extends Thread {
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
