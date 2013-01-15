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

package ca.uwaterloo.epad;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import javax.xml.transform.TransformerException;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import vialab.SMT.TouchClient;
import vialab.SMT.TouchDraw;
import vialab.SMT.TouchSource;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.painting.Brush;
import ca.uwaterloo.epad.painting.Eraser;
import ca.uwaterloo.epad.painting.Paint;
import ca.uwaterloo.epad.prompting.PromptManager;
import ca.uwaterloo.epad.ui.Button;
import ca.uwaterloo.epad.ui.Canvas;
import ca.uwaterloo.epad.ui.Container;
import ca.uwaterloo.epad.ui.Drawer;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.ui.SplashScreen;
import ca.uwaterloo.epad.util.DrawingPrinter;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.util.TTSManager;
import ca.uwaterloo.epad.xml.SimpleMarshaller;
import ca.uwaterloo.epad.xml.XmlAttribute;

public class Application extends PApplet {
	private static final long serialVersionUID = -1354251777507926593L;

	public static final int TOP_DRAWER = 0;
	public static final int LEFT_DRAWER = 1;
	public static final int RIGHT_DRAWER = 2;
	
	public static final String ITEM_ADDED = "item added";
	public static final String ITEM_REMOVED = "item removed";
	public static final String BRUSH_SELECTED = "brush selected";
	public static final String PAINT_SELECTED = "paint selected";

	// Fields
	@XmlAttribute public static int backgroundColour = 0xFFFFFFFF;
	@XmlAttribute public static String backgroundImage = null;

	// GUI components
	private static Brush selectedBrush;
	private static Paint selectedPaint;
	private static Drawer leftDrawer;
	private static Drawer rightDrawer;
	private static Drawer topDrawer;
	private static Canvas canvas;
	
	// Misc variables
	private static PFont font;
	private static ResourceBundle uiStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.UI", Settings.locale);
	private static PImage bg;
	private static long lastActionTime;
	
	private static ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	private static Application instance;
	
	private static ArrayList<Brush> brushes = new ArrayList<Brush>();
	private static ArrayList<Paint> paints = new ArrayList<Paint>();

	public void setup() {
		size(Settings.width, Settings.height, P3D);
		frameRate(Settings.targetFPS);
		smooth();
		
		instance = this;
		
		font = createDefaultFont(20);

		TouchClient.init(this, TouchSource.MOUSE);
		TouchClient.setWarnUnimplemented(false);
		TouchClient.setDrawTouchPoints(TouchDraw.SMOOTH, 0);
		
		try {
			SimpleMarshaller.unmarshallGui(this, new File(Settings.dataFolder + Settings.guiFile));
		} catch (IllegalArgumentException | IllegalAccessException | TransformerException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			exit();
		}

		if (backgroundImage != null && backgroundImage.length() > 0) {
			bg = this.loadImage(Settings.dataFolder + backgroundImage);
		}
		
		// create default canvas (in case it is not specified in the layout file
		setCanvas(new Canvas((width-800)/2, (height-600)/2, 800, 600, 255));
		
		loadLayout(Settings.defaultLayoutFile);
		
		// Put drawers on top
		if (leftDrawer != null)
			TouchClient.putZoneOnTop(leftDrawer);
		if (rightDrawer != null)
			TouchClient.putZoneOnTop(rightDrawer);
		if (topDrawer != null) {
			TouchClient.putZoneOnTop(topDrawer);
			
			// create control buttons
			makeControlPanel(topDrawer.getContainer());
		}
		
		setSelectedBrush(getAllBrushes().get(0));
		setSelectedPaint(getAllPaints().get(0));
		
		PromptManager.init(this);
		TTSManager.init();
		setActionPerformed();
		SplashScreen.remove();
	}

	public void draw() {
		background(backgroundColour);
		if (backgroundImage != null && backgroundImage.length() > 0) {
			imageMode(CORNER);
			image(bg, 0, 0, displayWidth, displayHeight);
		}
		
		if (Settings.showDebugInfo) {
			text(Math.round(frameRate) + "fps, # of zones: " + TouchClient.getZones().length, 10, 10);
			text("brushes: " + brushes.size() + ", paints: " + paints.size(), 10, 20);
		}
	}
	
	private static void makeControlPanel(Container c) {
		int w = 180;
		int h = 70;
		int x = 20;
		int y = c.height - h - 60;
		
		Button b = new Button(x, y, w, h, uiStrings.getString("SaveLayoutButton"), 20, font);
		b.setPressMethodByName("saveLayout", instance);
		c.addItem(b);
		
		x += w + 20;
		
		b = new Button(x, y, w, h, uiStrings.getString("SavePaintingButton"), 20, font);
		b.setPressMethodByName("saveDrawing", instance);
		c.addItem(b);
		
		x += w + 20;
		
		b = new Button(x, y, w, h, uiStrings.getString("ClearPaintingtButton"), 20, font);
		b.setPressMethodByName("clearCanvas", instance);
		c.addItem(b);
		
		x += w + 20;
		
		b = new Button(x, y, w, h, uiStrings.getString("LoadPaintingButton"), 20, font);
		b.setPressMethodByName("loadDrawing", instance);
		c.addItem(b);
		
		x += w + 20;
		
		b = new Button(x, y, w, h, uiStrings.getString("ToggleOverlayButton"), 20, font);
		b.setPressMethodByName("toggleOverlay", instance);
		c.addItem(b);
		
		x = 20;
		y -= h + 20;
		
		b = new Button(x, y, w, h, uiStrings.getString("PrintButton"), 20, font);
		b.setPressMethodByName("print", instance);
		c.addItem(b);
		
		x += w + 20;
		x += w + 20;
		x += w + 20;
		x += w + 20;
		
		b = new Button(x, y, w, h, uiStrings.getString("ExitButton"), 20, font);
		b.setPressMethodByName("exit", instance);
		c.addItem(b);
	}

	public void keyPressed() {
		if (key == ' ') {
			takeScreenshot();
		}
	}

	public static void setSelectedPaint(Paint p) {
		if (p == selectedPaint) return;
		
		if (selectedPaint != null)
			selectedPaint.deselect();
		selectedPaint = p;
		if (selectedPaint != null)
			selectedPaint.select();
		
		notifyListeners(p, PAINT_SELECTED);
	}

	public static Paint getSelectedPaint() {
		return selectedPaint;
	}
	
	public static ArrayList<Paint> getAllPaints() {
		return paints;
	}

	public static void setSelectedBrush(Brush b) {
		if (b == selectedBrush) return;
		
		if (selectedBrush != null)
			selectedBrush.deselect();
		selectedBrush = b;
		if (selectedBrush != null)
			selectedBrush.select();
		
		notifyListeners(b, BRUSH_SELECTED);
	}

	public static Brush getSelectedBrush() {
		return selectedBrush;
	}
	
	public static ArrayList<Brush> getAllBrushes() {
		return brushes;
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
		case LEFT_DRAWER: return leftDrawer;
		case RIGHT_DRAWER: return rightDrawer;
		default: return null;
		}
	}
	
	public static void setCanvas(Canvas newCanvas) {
		if (canvas != null) {
			TouchClient.remove(canvas);
		}
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
		else
			return false;
	}
	
	public static void setActionPerformed() {
		lastActionTime = System.currentTimeMillis();
	}
	
	public static long getInactiveTime() {
		return System.currentTimeMillis() - lastActionTime;
	}
	
	public static void addItem(MoveableItem item) {
		if (item instanceof Paint)
			paints.add((Paint)item);
		else if (item instanceof Brush && !(item instanceof Eraser))
			brushes.add((Brush)item);
		
		TouchClient.add(item);
		notifyListeners(item, ITEM_ADDED);
	}
	
	public static void removeItem(MoveableItem item) {
		if (item instanceof Paint)
			paints.remove(item);
		else if (!(item instanceof Eraser))
			brushes.remove(item);
		
		TouchClient.remove(item);
		notifyListeners(item, ITEM_REMOVED);
	}
	
	private static void notifyListeners(String message) {
		notifyListeners(instance, message);
	}
	
	private static void notifyListeners(Object source, String message) {
		for (int i = 0; i < listeners.size(); i++) {
			ActionListener listener = listeners.get(i);
			if (listener != null)
				listener.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_FIRST, message));
			else
				System.err.println("Application.notifyListeners(): null list element " + i);
		}
	}

	public static void addListener(ActionListener listener) {
		listeners.add(listener);
	}
	
	public static boolean removeListener(ActionListener listener) {
		return listeners.remove(listener);
	}
	
	public void saveLayout() {
		saveLayout("temp.xml");
	}
	
	public void saveLayout(String filename) {
		try {
			filename = Settings.dataFolder + filename;
			SimpleMarshaller.marshallLayout(new File(filename));
			System.out.println("Layout saved: " + filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadLayout(String filename) {
		try {
			SimpleMarshaller.unmarshallLayout(new File(Settings.dataFolder + filename));
		} catch (IllegalArgumentException | IllegalAccessException | TransformerException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public void takeScreenshot() {
		Date now = new Date();
		SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");

		String filename = "data\\screenshot_" + sdt.format(now) + ".png";

		PGraphics pg;
		pg = createGraphics(width, height, P3D);
		pg.beginDraw();
		draw();
		TouchClient.draw();
		PromptManager.draw();
		pg.endDraw();
		
		if (pg.save(filename))
			System.out.println("Screenshot saved: " + filename);
		else
			System.err.println("Failed to save screenshot");
	}
	
	public void saveDrawing() {
		Date now = new Date();
		SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");

		String filename = "data\\drawing_" + sdt.format(now) + ".png";
		
		if (canvas.getDrawing().save(filename))
			System.out.println("Drawing saved: " + filename);
		else
			System.err.println("Failed to save drawing");
	}
	
	public void clearCanvas() {
		canvas.clear();
	}
	
	public void loadDrawing() {
		canvas.clearAndLoad("data\\drawing_2012.12.19-22.23.48.466.png");
	}
	
	public void toggleOverlay() {
		canvas.toggleOverlay();
	}
	
	public void print() {
		new Thread(new DrawingPrinter(canvas.getDrawing(), Settings.showPrintDialog)).start();
	}
	
	public void exit() {
		TTSManager.dispose();
		super.exit();
	}
}
