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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

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
import ca.uwaterloo.epad.ui.ApplicationState;
import ca.uwaterloo.epad.ui.Button;
import ca.uwaterloo.epad.ui.Canvas;
import ca.uwaterloo.epad.ui.Container;
import ca.uwaterloo.epad.ui.Drawer;
import ca.uwaterloo.epad.ui.FileBrowserDialog;
import ca.uwaterloo.epad.ui.FileBrowserDialog.FileButton;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.ui.ResetDialog;
import ca.uwaterloo.epad.ui.SaveDialog;
import ca.uwaterloo.epad.ui.SplashScreen;
import ca.uwaterloo.epad.util.DrawingPrinter;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.util.TTSManager;
import ca.uwaterloo.epad.xml.SaveFile;
import ca.uwaterloo.epad.xml.SimpleMarshaller;
import ca.uwaterloo.epad.xml.XmlAttribute;

/**
 * This is the main class of ePad application that contains the drawing loop and
 * manages the GUI components such as Canvas and Drawers. It also handles user
 * interactions with the buttons in the top drawer: Save, Load, Clear, Reset,
 * Colouring Pages and Print.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class Application extends PApplet implements ActionListener {
	private static final long serialVersionUID = -1354251777507926593L;
	private static final Logger LOGGER = Logger.getLogger(Application.class);

	// Drawer location identifiers
	public static final int TOP_DRAWER = 0;
	public static final int LEFT_DRAWER = 1;
	public static final int RIGHT_DRAWER = 2;

	/**
	 * The event ID for "an item was added" event.
	 */
	public static final String ITEM_ADDED = "item added";
	/**
	 * The event ID for "an item was removed" event.
	 */
	public static final String ITEM_REMOVED = "item removed";
	/**
	 * The event ID for "a brush was selected" event.
	 */
	public static final String BRUSH_SELECTED = "brush selected";
	/**
	 * The event ID for "a paint was selected" event.
	 */
	public static final String PAINT_SELECTED = "paint selected";

	// Colour scheme
	@XmlAttribute
	public static int backgroundColour = 0xFFFFFFFF;
	@XmlAttribute
	public static String backgroundImage = null;
	@XmlAttribute
	public static int primaryColour = 0;
	@XmlAttribute
	public static int secondaryColour = 0;
	@XmlAttribute
	public static int textColour = 0;
	@XmlAttribute
	public static int transparentColour = 0;
	@XmlAttribute
	public static int transparentAlpha = 0;
	@XmlAttribute
	public static int deleteColour = 0;

	// GUI components
	private static Brush selectedBrush;
	private static Paint selectedPaint;
	private static Drawer leftDrawer;
	private static Drawer rightDrawer;
	private static Drawer topDrawer;
	private static Canvas canvas;

	// Bundle of UI strings
	private static final ResourceBundle uiStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.UI", Settings.locale);

	// Other variables
	private static PFont font;
	private static PImage bg;
	private static long lastActionTime;
	private static ApplicationState state;

	// Array of event listeners
	private static ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	// Instance of the application
	private static Application instance;

	// Array of brushes that are currently on the screen
	private static ArrayList<Brush> brushes = new ArrayList<Brush>();
	// Array of paints that are currently on the screen
	private static ArrayList<Paint> paints = new ArrayList<Paint>();

	/**
	 * Perform the initial setup: set size, initialise TouchClient, load GUI and
	 * layout, initialise PromptManager and TTSManager.
	 */
	@Override
	public void setup() {
		size(Settings.width, Settings.height, P3D);
		frameRate(Settings.targetFPS);
		LOGGER.info("Application started with parameters: width=" + Settings.width + ", height=" + Settings.height + ", fps=" + Settings.targetFPS);

		instance = this;

		font = createDefaultFont(20);

		// Figure out the touch source
		TouchSource source;
		String sourceString = Settings.touchSourse.toUpperCase();
		if ("TUIO_DEVICE".equals(sourceString))
			source = TouchSource.TUIO_DEVICE;
		else if ("MOUSE".equals(sourceString))
			source = TouchSource.MOUSE;
		else if ("WM_TOUCH".equals(sourceString))
			source = TouchSource.WM_TOUCH;
		else if ("ANDROID".equals(sourceString))
			source = TouchSource.ANDROID;
		else if ("SMART".equals(sourceString))
			source = TouchSource.SMART;
		else
			source = TouchSource.MOUSE;

		TouchClient.init(this, source);
		TouchClient.setWarnUnimplemented(false);
		TouchClient.setDrawTouchPoints(TouchDraw.SMOOTH, 0);

		loadGUI();
		loadLayout(Settings.dataFolder + Settings.defaultLayoutFile);

		PromptManager.init(this);
		TTSManager.init();
		setActionPerformed();
		state = ApplicationState.IDLE;
		SplashScreen.remove();
	}

	/**
	 * Draw loop. Only background and debug information is drawn here,
	 * everything else is drawn by TouchClient and TTSManager.
	 */
	@Override
	public void draw() {
		background(backgroundColour);
		if (backgroundImage != null && backgroundImage.length() > 0 && bg != null) {
			imageMode(CORNER);
			image(bg, 0, 0, displayWidth, displayHeight);
		}

		if (Settings.showDebugInfo) {
			String s1 = Math.round(frameRate) + "fps, # of zones: " + TouchClient.getZones().length;
			String s2 = "brushes: " + brushes.size() + ", paints: " + paints.size();

			String s3 = "state: ";
			switch (state) {
			case RUNNING:
				s3 += "Running";
				break;
			case IDLE:
				s3 += "idle";
				break;
			case PAUSED:
				s3 += "paused";
				break;
			}

			text(s1, 10, 10);
			text(s2, 10, 20);
			text(s3, 10, 30);

			text(s1, 10, height - 30);
			text(s2, 10, height - 20);
			text(s3, 10, height - 10);

			text(s1, width - 150, 10);
			text(s2, width - 150, 20);
			text(s3, width - 150, 30);

			text(s1, width - 150, height - 30);
			text(s2, width - 150, height - 20);
			text(s3, width - 150, height - 10);
		}

		if (getInactiveTime() >= Settings.resetPromptDelay && !ResetDialog.isOnScreen())
			TouchClient.add(new ResetDialog());
	}

	/**
	 * Display the application in full screen mode.
	 */
	@Override
	public boolean sketchFullScreen() {
		return true;
	}

	// Add buttons to the top drawer
	private static void makeControlPanel(Container c) {
		int w = 180;
		int h = 70;
		int x = 20;
		int y = c.height - h - 60;

		Button b = new Button(x, y, w, h, uiStrings.getString("SavePaintingButton"), 20, font);
		b.setStaticPressMethod("save", Application.class);
		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
		c.addItem(b);

		x += w + 20;

		b = new Button(x, y, w, h, uiStrings.getString("LoadPaintingButton"), 20, font);
		b.setStaticPressMethod("load", Application.class);
		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
		c.addItem(b);

		x += w + 20;

		b = new Button(x, y, w, h, uiStrings.getString("ClearPaintingButton"), 20, font);
		b.setStaticPressMethod("clearCanvas", Application.class);
		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
		c.addItem(b);

		x += w + 20;

		b = new Button(x, y, w, h, uiStrings.getString("ResetButton"), 20, font);
		b.setStaticPressMethod("resetToDefaults", Application.class);
		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
		c.addItem(b);

		x += w + 20;

		b = new Button(x, y, w, h, uiStrings.getString("ColouringPagesButton"), 20, font);
		b.setStaticPressMethod("colouringMode", Application.class);
		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
		c.addItem(b);

		x += w + 20;

		b = new Button(x, y, w, h, uiStrings.getString("PrintButton"), 20, font);
		b.setStaticPressMethod("print", Application.class);
		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
		c.addItem(b);

		/*
		 * x = instance.width - w - 20;
		 * 
		 * b = new Button(x, y, w, h, uiStrings.getString("ExitButton"), 30,
		 * instance.createFont("Arial", 30));
		 * b.setStaticPressMethod("closeApplication", Application.class);
		 * b.setColourScheme(0xFFCC0000, 0xFFFF4444, 0xFFFF4444); c.addItem(b);
		 */
	}

	/**
	 * Handle key press events. Take a screenshot when a "space" key is pressed.
	 */
	@Override
	public void keyPressed() {
		if (key == ' ') {
			takeScreenshot();
		}
	}

	/**
	 * 
	 * @return the current state of the application
	 * @see ApplicationState
	 */
	public static ApplicationState getState() {
		return state;
	}

	/**
	 * Pause the application.
	 */
	public static void pauseApplication() {
		state = ApplicationState.PAUSED;
		PromptManager.pause();
	}

	/**
	 * Resume the application.
	 */
	public static void resumeApplication() {
		if (state == ApplicationState.PAUSED) {
			PromptManager.resume();
		} else if (state == ApplicationState.IDLE) {
			LOGGER.info("Application was resumed from idle state");
			PromptManager.reset();
		}

		state = ApplicationState.RUNNING;
	}

	/**
	 * Put the application into idle state.
	 */
	public static void idleApplication() {
		LOGGER.info("Application is idle");
		state = ApplicationState.IDLE;
		PromptManager.pause();
	}

	/**
	 * Set the provided Paint object as currently selected.
	 * 
	 * @param p
	 *            Paint object to mark as selected
	 */
	public static void setSelectedPaint(Paint p) {
		if (p == selectedPaint)
			return;

		if (selectedPaint != null)
			selectedPaint.deselect();
		selectedPaint = p;
		if (selectedPaint != null)
			selectedPaint.select();

		notifyListeners(p, PAINT_SELECTED);
	}

	/**
	 * 
	 * @return the currently selected Paint object
	 */
	public static Paint getSelectedPaint() {
		return selectedPaint;
	}

	/**
	 * 
	 * @return the array of all Paint objects on the screen.
	 */
	public static ArrayList<Paint> getAllPaints() {
		return paints;
	}

	/**
	 * Set the provided Brush object as currently selected.
	 * 
	 * @param b
	 *            Brush object to mark as selected
	 */
	public static void setSelectedBrush(Brush b) {
		if (b == selectedBrush)
			return;

		if (selectedBrush != null)
			selectedBrush.deselect();
		selectedBrush = b;
		if (selectedBrush != null)
			selectedBrush.select();

		notifyListeners(b, BRUSH_SELECTED);
	}

	/**
	 * 
	 * @return the currently selected Brush object
	 */
	public static Brush getSelectedBrush() {
		return selectedBrush;
	}

	/**
	 * 
	 * @return the array of all Brush objects on the screen (except for Eraser).
	 */
	public static ArrayList<Brush> getAllBrushes() {
		return brushes;
	}

	/**
	 * 
	 * @return the array of all Zone objects currently registered with the
	 *         TouchClient.
	 */
	public static Zone[] getChildren() {
		return TouchClient.getZones();
	}

	/**
	 * Set the provided drawer to a certain position.
	 * 
	 * @param newDrawer
	 *            new drawer to add to the GUI
	 * @param drawerId
	 *            position of the new drawer, can be on of the following:
	 *            {@link #TOP_DRAWER}, {@link #LEFT_DRAWER} or
	 *            {@link #RIGHT_DRAWER}
	 */
	public static void setDrawer(Drawer newDrawer, int drawerId) {
		switch (drawerId) {
		case TOP_DRAWER:
			if (topDrawer != null)
				TouchClient.remove(topDrawer);
			topDrawer = newDrawer;
			break;
		case LEFT_DRAWER:
			if (leftDrawer != null)
				TouchClient.remove(leftDrawer);
			leftDrawer = newDrawer;
			break;
		case RIGHT_DRAWER:
			if (rightDrawer != null)
				TouchClient.remove(rightDrawer);
			rightDrawer = newDrawer;
			break;
		default:
			return;
		}
		TouchClient.add(newDrawer);
	}

	/**
	 * Get the drawer in a certain position.
	 * 
	 * @param drawerId
	 *            position of the drawer, can be on of the following:
	 *            {@link #TOP_DRAWER}, {@link #LEFT_DRAWER} or
	 *            {@link #RIGHT_DRAWER}
	 * @return the drawer registered in the provided position or <b>null</b> if
	 *         no drawer was added to that position
	 */
	public static Drawer getDrawer(int drawerId) {
		switch (drawerId) {
		case TOP_DRAWER:
			return topDrawer;
		case LEFT_DRAWER:
			return leftDrawer;
		case RIGHT_DRAWER:
			return rightDrawer;
		default:
			return null;
		}
	}

	/**
	 * Add a canvas to GUI.
	 * 
	 * @param newCanvas
	 *            new Canvas to be added to the screen
	 */
	public static void setCanvas(Canvas newCanvas) {
		if (canvas != null) {
			TouchClient.remove(canvas);
		}
		canvas = newCanvas;

		TouchClient.add(canvas);
	}

	/**
	 * Get the current Canvas.
	 * 
	 * @return the current Canvas
	 */
	public static Canvas getCanvas() {
		return canvas;
	}

	/**
	 * Determine if the item is above on of the drawers.
	 * 
	 * @param item
	 *            the item to check
	 * @return <b>true</b> if the item is above on of the drawers and
	 *         <b>false</b> otherwise
	 * @see Drawer#isItemAbove(Zone)
	 */
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

	/**
	 * This method should be called each time user touched the screen. It tracks
	 * saves the time of the last action, which helps to determine how long the
	 * application has been inactive.
	 * 
	 */
	public static void setActionPerformed() {
		if (state == ApplicationState.IDLE) {
			state = ApplicationState.RUNNING;
			PromptManager.reset();
		}
		lastActionTime = System.currentTimeMillis();
	}

	/**
	 * 
	 * @return the period of time that the application has been inactive in
	 *         milliseconds.
	 */
	public static long getInactiveTime() {
		return System.currentTimeMillis() - lastActionTime;
	}

	/**
	 * Add an item to the screen.
	 * 
	 * @param item
	 *            the item to add
	 */
	public static void addItem(MoveableItem item) {
		if (item instanceof Paint)
			paints.add((Paint) item);
		else if (item instanceof Brush && !(item instanceof Eraser))
			brushes.add((Brush) item);

		TouchClient.add(item);
		notifyListeners(item, ITEM_ADDED);
	}

	/**
	 * Remove an item from the screen.
	 * 
	 * @param item
	 *            the item to remove
	 */
	public static void removeItem(MoveableItem item) {
		if (item instanceof Paint)
			paints.remove(item);
		else if (!(item instanceof Eraser))
			brushes.remove(item);

		TouchClient.remove(item);
		notifyListeners(item, ITEM_REMOVED);
	}

	// Send a message to all listeners
	private static void notifyListeners(Object source, String message) {
		for (int i = 0; i < listeners.size(); i++) {
			ActionListener listener = listeners.get(i);
			if (listener != null)
				listener.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_FIRST, message));
			else
				LOGGER.error("A listener is null: " + i);
		}
	}

	/**
	 * Add a listener.
	 * 
	 * @param listener
	 *            listener object to add
	 */
	public static void addListener(ActionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Remove a listener.
	 * 
	 * @param listener
	 *            listener object to remove
	 * @return <b>true</b> if the list of listeners contained the specified
	 *         object
	 */
	public static boolean removeListener(ActionListener listener) {
		return listeners.remove(listener);
	}

	/**
	 * Load the GUI from a file.
	 * 
	 * @see SimpleMarshaller#unmarshallGui(Application, File)
	 */
	public static void loadGUI() {
		LOGGER.info("Loading GUI from file: " + Settings.dataFolder + Settings.guiFile);

		try {
			SimpleMarshaller.unmarshallGui(instance, new File(Settings.dataFolder + Settings.guiFile));
		} catch (Exception e) {
			LOGGER.fatal("Failed to load GUI. " + e.getLocalizedMessage());
			System.exit(1);
		}

		if (backgroundImage != null && backgroundImage.length() > 0) {
			bg = instance.loadImage(Settings.dataFolder + backgroundImage);
			if (bg == null)
				LOGGER.error("Failed to load image: " + Settings.dataFolder + backgroundImage);
		}

		// Create default canvas (in case it is not specified in the layout file
		setCanvas(new Canvas((instance.width - 800) / 2, (instance.height - 600) / 2, 800, 600, 255));

		// Create control buttons
		makeControlPanel(topDrawer.getContainer());
	}

	/**
	 * Clear the entire workspace by removing all zones and then loading the GUI
	 * again.
	 */
	public static void clearWorkspace() {
		LOGGER.info("Clearing workspace");

		brushes.clear();
		paints.clear();

		// remove all zones
		Zone[] zones = TouchClient.getZones();
		for (int i = 0; i < zones.length; i++) {
			Zone z = zones[i];
			TouchClient.remove(z);
		}

		loadGUI();
	}

	/**
	 * Load a save file, including the layout and painting.
	 * 
	 * @param save
	 *            SaveFile object to load data from
	 */
	public static void loadSave(SaveFile save) {
		LOGGER.info("Loading a save file " + save.filename);

		clearWorkspace();
		loadLayout(save.layoutPath);
		canvas.clearAndLoad(save.drawingPath);

		// Put drawers on top
		if (leftDrawer != null)
			TouchClient.putZoneOnTop(leftDrawer);
		if (rightDrawer != null)
			TouchClient.putZoneOnTop(rightDrawer);
		if (topDrawer != null) {
			TouchClient.putZoneOnTop(topDrawer);
		}

		setSelectedBrush(getAllBrushes().get(0));
		setSelectedPaint(getAllPaints().get(0));

		PromptManager.reset();
	}

	/**
	 * Save the current workspace layout into the provided file.
	 * 
	 * @param filename
	 *            valid file name to save layout into
	 */
	public static void saveLayout(String filename) {
		LOGGER.info("Saving layout to file: " + filename);

		try {
			SimpleMarshaller.marshallLayout(new File(filename));
		} catch (Exception e) {
			LOGGER.error("Failed to save layout. " + e.getLocalizedMessage());
		}
	}

	/**
	 * Load a workspace layout from the provided file.
	 * 
	 * @param filename
	 *            valid file that contains a workspace layout
	 */
	public static void loadLayout(String filename) {
		LOGGER.info("Loading layout from file: " + filename);

		try {
			SimpleMarshaller.unmarshallLayout(new File(filename));
		} catch (Exception e) {
			LOGGER.error("Failed to load layout. " + e.getLocalizedMessage());
		}

		// Put drawers on top
		if (leftDrawer != null)
			TouchClient.putZoneOnTop(leftDrawer);
		if (rightDrawer != null)
			TouchClient.putZoneOnTop(rightDrawer);
		if (topDrawer != null) {
			TouchClient.putZoneOnTop(topDrawer);
		}

		setSelectedBrush(getAllBrushes().get(0));
		setSelectedPaint(getAllPaints().get(0));
	}

	/**
	 * Take a screenshot and save it using the current time as the file name.
	 */
	public static void takeScreenshot() {
		Date now = new Date();
		SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");

		String filename = "data\\screenshot_" + sdt.format(now) + ".png";

		PGraphics pg;
		pg = instance.createGraphics(instance.width, instance.height, P3D);
		pg.beginDraw();
		instance.draw();
		TouchClient.draw();
		PromptManager.draw();
		pg.endDraw();

		if (pg.save(filename))
			LOGGER.info("Screenshot saved: " + filename);
		else
			LOGGER.error("Failed to save screenshot.");
	}

	/**
	 * Show the save dialog.
	 * 
	 * @see SaveDialog
	 */
	public static void save() {
		TouchClient.add(new SaveDialog());
	}

	/**
	 * Show a file browser set to the save folder.
	 * 
	 * @see FileBrowserDialog
	 */
	public static void load() {
		FileBrowserDialog saveFileBrowser = new FileBrowserDialog(uiStrings.getString("SaveFileBrowserHeaderText"), Settings.saveFolder, SaveFile.SAVE_FILE_EXT, Settings.fileBrowserColumns,
				Settings.fileBrowserRows);
		saveFileBrowser.addListener(instance);
		TouchClient.add(saveFileBrowser);
	}

	/**
	 * Clear the canvas.
	 * 
	 * @see Canvas#clear()
	 */
	public static void clearCanvas() {
		LOGGER.info("Clear canvas.");
		if (canvas != null)
			canvas.clear();
	}

	/**
	 * Reset the workspace to defaults: clear the workspace and then load the
	 * default layout.
	 */
	public static void resetToDefaults() {
		LOGGER.info("Reset workspace and load defaults.");
		clearWorkspace();
		loadLayout(Settings.dataFolder + Settings.defaultLayoutFile);
		// Put drawers on top
		if (leftDrawer != null)
			TouchClient.putZoneOnTop(leftDrawer);
		if (rightDrawer != null)
			TouchClient.putZoneOnTop(rightDrawer);
		if (topDrawer != null) {
			TouchClient.putZoneOnTop(topDrawer);
		}

		setSelectedBrush(getAllBrushes().get(0));
		setSelectedPaint(getAllPaints().get(0));
		setActionPerformed();

		PromptManager.reset();
		idleApplication();
	}

	/**
	 * Start the colouring mode: show a file browser set to the colouring
	 * folder.
	 * 
	 * @see FileBrowserDialog
	 */
	public static void colouringMode() {
		FileBrowserDialog imageFileBrowser = new FileBrowserDialog(uiStrings.getString("ImageFileBrowserHeaderText"), Settings.colouringFolder, ".png", Settings.fileBrowserColumns,
				Settings.fileBrowserRows);
		imageFileBrowser.addListener(instance);
		TouchClient.add(imageFileBrowser);
	}

	/**
	 * Print the current painting.
	 * 
	 * @see DrawingPrinter
	 */
	public static void print() {
		new Thread(new DrawingPrinter(canvas.getDrawing(false), Settings.showPrintDialog)).start();
	}

	/**
	 * Close the application.
	 */
	public static void closeApplication() {
		LOGGER.info("Application closing.");
		TTSManager.dispose();
		instance.exit();
	}

	/**
	 * Listen to the events sent by FileBrowserDialog and handle them
	 * appropriately.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(FileBrowserDialog.FILE_SELECTED)) {
			FileButton fb = (FileButton) e.getSource();
			if (fb.fileType == FileBrowserDialog.FILE_TYPE_IMAGE) {
				getCanvas().setOverlayImage(fb.filePath);
			} else if (fb.fileType == FileBrowserDialog.FILE_TYPE_SAVE) {
				loadSave(fb.save);
			}
		}
	}
}
