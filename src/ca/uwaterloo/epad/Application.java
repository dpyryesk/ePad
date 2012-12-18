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
import ca.uwaterloo.epad.ui.SlidingDrawer;
import ca.uwaterloo.epad.xml.SimpleMarshaller;
import ca.uwaterloo.epad.xml.XmlAttribute;

public class Application extends PApplet {
	private static final long serialVersionUID = -1354251777507926593L;

	public static final int TOP_DRAWER = 0;
	public static final int LEFT_DRAWER = 1;
	public static final int RIGHT_DRAWER = 2;
	public static final int BOTTOM_DRAWER = 3;

	// Fields
	public int width = 1024;
	public int height = 768;
	public float targetFPS = 60;
	@XmlAttribute public int backgroundColour = 0xFFFFFFFF;
	@XmlAttribute public String backgroundImage = null;

	private static PImage bg;

	// GUI
	public static Brush currentBrush;
	public static Paint currentPaint;
	public static Drawer leftDrawer;
	public static Drawer rightDrawer;
	public static Drawer topDrawer;
	public static Drawer bottomDrawer;
	public static Canvas canvas;

	// XML file paths
	private final static String settingsFile = "data\\settings.xml";
	private final static String guiFile = "data\\gui.xml";
	private final static String defaultLayoutFile = "data\\layout.xml";

	public void setup() {
		size(width, height, P3D);
		frameRate(targetFPS);
		smooth();

		TouchClient.init(this, TouchSource.MOUSE);
		TouchClient.setWarnUnimplemented(false);
		TouchClient.setDrawTouchPoints(true, 0);

		canvas = new Canvas((width-800)/2, (height-600)/2, 800, 600);
		TouchClient.add(canvas);

		try {
			SplashScreen.setMessage("Loading GUI...");
			SimpleMarshaller.unmarshallGui(this, new File(guiFile));
			SplashScreen.setMessage("Loading Layout...");
			SimpleMarshaller.unmarshallLayout(this, new File(defaultLayoutFile));
		} catch (IllegalArgumentException | IllegalAccessException | TransformerException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			exit();
		}

		if (backgroundImage != null && backgroundImage.length() > 0) {
			bg = this.loadImage(backgroundImage);
		}
		
		addDrawer(SlidingDrawer.makeTopDrawer(this), TOP_DRAWER);
		
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
				SimpleMarshaller.marshallLayout(this, new File(defaultLayoutFile));
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

	public Zone[] getChildren() {
		return TouchClient.getZones();
	}

	public static void addDrawer(Drawer drawer, int drawerId) {
		switch (drawerId) {
		case TOP_DRAWER:
			topDrawer = drawer;
			break;
		case BOTTOM_DRAWER:
			bottomDrawer = drawer;
			break;
		case LEFT_DRAWER:
			leftDrawer = drawer;
			break;
		case RIGHT_DRAWER:
			rightDrawer = drawer;
			break;
		}
		TouchClient.add(drawer);
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
}
