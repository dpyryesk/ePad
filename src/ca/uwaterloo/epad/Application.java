package ca.uwaterloo.epad;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.TransformerException;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import vialab.SMT.TouchClient;
import vialab.SMT.TouchSource;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.painting.Brush;
import ca.uwaterloo.epad.painting.Canvas;
import ca.uwaterloo.epad.painting.Paint;
import ca.uwaterloo.epad.ui.Drawer;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.xml.SimpleMarshaller;
import ca.uwaterloo.epad.xml.XmlAttribute;

public class Application extends PApplet {
	private static final long serialVersionUID = -1354251777507926593L;

	public static final int TOP_DRAWER = 0;
	public static final int LEFT_DRAWER = 1;
	public static final int RIGHT_DRAWER = 2;
	public static final int BOTTOM_DRAWER = 3;

	// Fields
	public static int width = 1024;
	public static int height = 768;
	public static float targetFPS = 60;
	@XmlAttribute public static int backgroundColour = 0xFFFFFFFF;
	@XmlAttribute public static String backgroundImage = null;

	private static PImage bg;

	// GUI components
	private static Brush currentBrush;
	private static Paint currentPaint;
	private static Drawer leftDrawer;
	private static Drawer rightDrawer;
	private static Drawer topDrawer;
	private static Drawer bottomDrawer;
	private static Canvas canvas;

	// XML file paths
	private final static String guiFile = "data\\gui.xml";
	private final static String defaultLayoutFile = "data\\layout.xml";

	public void setup() {
		size(width, height, P3D);
		frameRate(targetFPS);
		smooth();

		TouchClient.init(this, TouchSource.MOUSE);
		TouchClient.setWarnUnimplemented(false);
		TouchClient.setDrawTouchPoints(true, 0);
		
		// create default canvas
		setCanvas(new Canvas((width-800)/2, (height-600)/2, 800, 600, 255));

		try {
			SplashScreen.setMessage("Loading GUI...");
			SimpleMarshaller.unmarshallGui(this, new File(guiFile));
			SplashScreen.setMessage("Loading Layout...");
			SimpleMarshaller.unmarshallLayout(new File(defaultLayoutFile));
		} catch (IllegalArgumentException | IllegalAccessException | TransformerException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			exit();
		}

		if (backgroundImage != null && backgroundImage.length() > 0) {
			bg = this.loadImage(backgroundImage);
		}
		
		if (canvas == null) {
			
		}
		
		// Put drawers on top
		if (leftDrawer != null)
			TouchClient.putZoneOnTop(leftDrawer);
		if (rightDrawer != null)
			TouchClient.putZoneOnTop(rightDrawer);
		if (topDrawer != null)
			TouchClient.putZoneOnTop(topDrawer);
		if (bottomDrawer != null)
			TouchClient.putZoneOnTop(bottomDrawer);
		
		SplashScreen.remove();
	}

	public void draw() {
		background(backgroundColour);
		if (backgroundImage != null && backgroundImage.length() > 0) {
			imageMode(CORNER);
			image(bg, 0, 0, displayWidth, displayHeight);
		}
		
		text(Math.round(frameRate) + "fps, # of zones: " + TouchClient.getZones().length, 10, 10);
	}

	public final void keyPressed() {
		if (key == '`') {
			Date now = new Date();
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");

			String filename = "data\\screenshot_" + sdt.format(now) + ".png";

			PGraphics pg;
			pg = createGraphics(width, height, P3D);
			pg.beginDraw();
			draw();
			TouchClient.draw();
			pg.endDraw();
			
			if (pg.save(filename))
				System.out.println("Screenshot saved: " + filename);
			else
				System.err.println("Failed to save screenshot");
		} else if (key == ' ') {
			Date now = new Date();
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");

			String filename = "data\\drawing_" + sdt.format(now) + ".png";

			if (canvas.getDrawing().save(filename))
				System.out.println("Drawing saved: " + filename);
			else
				System.err.println("Failed to save drawing");
		} else if (key == 's') {
			try {
				SimpleMarshaller.marshallLayout(new File(defaultLayoutFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void setPaint(Paint p) {
		if (currentPaint != null)
			currentPaint.deselect();
		currentPaint = p;
		if (currentPaint != null)
			currentPaint.select();
	}

	public static Paint getPaint() {
		return currentPaint;
	}

	public static void setBrush(Brush b) {
		if (currentBrush != null)
			currentBrush.deselect();
		currentBrush = b;
		if (currentBrush != null)
			currentBrush.select();
	}

	public static Brush getBrush() {
		return currentBrush;
	}

	public static Zone[] getChildren() {
		return TouchClient.getZones();
	}

	public static void setDrawer(Drawer newDrawer, int drawerId) {
		switch (drawerId) {
		case TOP_DRAWER:
			if (topDrawer != null) TouchClient.remove(topDrawer);
			topDrawer = newDrawer;
			break;
		case BOTTOM_DRAWER:
			if (bottomDrawer != null) TouchClient.remove(bottomDrawer);
			bottomDrawer = newDrawer;
			break;
		case LEFT_DRAWER:
			if (leftDrawer != null) TouchClient.remove(leftDrawer);
			leftDrawer = newDrawer;
			break;
		case RIGHT_DRAWER:
			if (rightDrawer != null) TouchClient.remove(rightDrawer);
			rightDrawer = newDrawer;
			break;
		default: return;
		}
		TouchClient.add(newDrawer);
	}
	
	public static Drawer getDrawer(int drawerId) {
		switch(drawerId) {
		case TOP_DRAWER: return topDrawer;
		case BOTTOM_DRAWER: return bottomDrawer;
		case LEFT_DRAWER: return leftDrawer;
		case RIGHT_DRAWER: return rightDrawer;
		default: return null;
		}
	}
	
	public static void setCanvas(Canvas newCanvas) {
		if (canvas != null) TouchClient.remove(canvas);
		canvas = newCanvas;
		TouchClient.add(canvas);
	}
	
	public static Canvas getCanvas() {
		return canvas;
	}
	
	public static boolean isItemAboveDrawer(MoveableItem item) {
		if (leftDrawer != null && leftDrawer.isItemAbove(item))
			return true;
		else if (rightDrawer != null && rightDrawer.isItemAbove(item))
			return true;
		else if (topDrawer != null && topDrawer.isItemAbove(item))
			return true;
		else if (bottomDrawer != null && bottomDrawer.isItemAbove(item))
			return true;
		else
			return false;
	}
	
	public static boolean isDrawerOpen() {
		if (leftDrawer != null && leftDrawer.isOpen())
			return true;
		else if (rightDrawer != null && rightDrawer.isOpen())
			return true;
		else if (topDrawer != null && topDrawer.isOpen())
			return true;
		else if (bottomDrawer != null && bottomDrawer.isOpen())
			return true;
		else
			return false;
	}
}
