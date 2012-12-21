package ca.uwaterloo.epad;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import processing.core.PImage;

public class DrawingPrinter implements Printable {
	private static PImage img;

	public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
		if (page > 0) {
			return NO_SUCH_PAGE;
		}
		
		//Graphics2D g2d = (Graphics2D) g;
		//g2d.translate(pf.getImageableX(), pf.getImageableY());
		g.translate(0, 0);
		int formh = (int) pf.getHeight();
		int formw = (int) pf.getWidth();

		g.drawImage((Image) img.getNative(), 0, 0, formw, formh, null);

		return PAGE_EXISTS;
	}

	public static void printDrawing(PImage drawing) {
		img = drawing;
		PrinterJob pjob = PrinterJob.getPrinterJob();
		Book book = new Book();
		
		PageFormat landscape = pjob.defaultPage();
		landscape.setOrientation(PageFormat.LANDSCAPE);
		book.append(new DrawingPrinter(), landscape);
		
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
	
	public static void printDrawingWithoutDialog(PImage drawing) {
		img = drawing;
		PrinterJob pjob = PrinterJob.getPrinterJob();
		Book book = new Book();
		
		PageFormat landscape = pjob.defaultPage();
		landscape.setOrientation(PageFormat.LANDSCAPE);
		book.append(new DrawingPrinter(), landscape);
		
		pjob.setPageable(book);
		try {
			pjob.print();
		} catch (PrinterException e) {
			e.printStackTrace();
		}
	}
}
