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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.xml.SaveFile;

/**
 * This class implements file browsing functionality for a certain folder, file
 * extension filter can also be specified. When a file is selected, the browser
 * dialog will close and send a notification to its listeners.</br>File browser
 * can handle image files (see the note below) and save files created by ePad
 * application. All other files will be displayed as text, and thumbnails will
 * not be generated.</br><b>Important:</b> Only 4 types of images are supported:
 * .gif, .jpg, .tga and .png (this is a limitation of Processing framework).
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see FileButton
 */
public class FileBrowserDialog extends Zone {
	private static final Logger LOGGER = Logger.getLogger(FileBrowserDialog.class);
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd yyyy, hh:mm aaa");

	/**
	 * The event ID for "a file was selected" event.
	 */
	public static final String FILE_SELECTED = "file selected";

	public static final int FILE_TYPE_IMAGE = 0;
	public static final int FILE_TYPE_SAVE = 1;
	public static final int FILE_TYPE_OTHER = 2;

	// Colours
	public int backgroundColour = Application.backgroundColour;
	public int borderColour = Application.primaryColour;
	public int transparentColour = Application.transparentColour;
	public int transparentAlpha = Application.transparentAlpha;
	public int textColour = Application.textColour;
	public int headerColour = Application.secondaryColour;

	// Layout parameters
	private int outerPadding = 70;
	private int innerPadding = 30;
	private int headerSize = 50;
	private int itemHeaderSize = 20;
	private int dialogWidth, dialogHeight;

	// Layout parameters for file buttons
	private int currentRow, currentColumn, currentPage;
	private int maxColumns;
	private int maxRows;
	private int maxPages;
	private int itemsPerPage;
	private float itemWidth;
	private float itemHeight;

	// Other variables
	private String headerText;
	private String folderPath;
	private String extension;
	private ArrayList<File> files;
	private static PFont headerFont, itemFont;
	private IconButton leftArrow, rightArrow;

	// Array of event listeners
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	// Flag indicating that the dialog is currently on screen
	private static boolean isOnScreen;

	/**
	 * Default constructor.
	 * 
	 * @param header
	 *            text to be displayed in the header of the dialog
	 * @param folder
	 *            path to the folder from which the files should be retrieved
	 * @param extension
	 *            filter files by extension, should include the '.' (i.e.
	 *            ".png", ".sav"), set to <b>null</b> to disable filtering by
	 *            extension
	 * @param columns
	 *            number of columns of files in the dialog
	 * @param rows
	 *            number of rows of files in the dialog
	 */
	public FileBrowserDialog(String header, String folder, String extension, int columns, int rows) {
		super(0, 0, applet.width, applet.height);

		LOGGER.info("FileBrowser opened: folder=" + folder + ", extension=" + extension);

		// Pause the application while the dialog is on the screen
		Application.pauseApplication();
		isOnScreen = true;

		// Calculate the dimensions based on widget size and padding
		dialogWidth = applet.width - outerPadding * 2;
		dialogHeight = applet.height - outerPadding * 2;

		this.folderPath = folder;
		this.extension = extension;
		this.headerText = header;
		this.maxColumns = columns;
		this.maxRows = rows;
		itemsPerPage = maxColumns * maxRows;

		// Create fonts
		headerFont = applet.createFont("Arial", headerSize);
		itemFont = applet.createFont("Arial", itemHeaderSize);
		
		// Create navigation buttons
		leftArrow = new IconButton(outerPadding - 50, applet.height / 2 - 75 / 2, 75, 75, "arrow_left", borderColour, backgroundColour);
		leftArrow.setPressMethod("prevPage", this);
		rightArrow = new IconButton(applet.width - outerPadding - 25, applet.height / 2 - 75 / 2, 75, 75, "arrow_right", borderColour, backgroundColour);
		rightArrow.setPressMethod("nextPage", this);

		getFiles();
		showPage();

		// Add a close button
		add(new CloseButton(dialogWidth + outerPadding - 50, outerPadding - 25, 75, 75));
	}

	// Draw the dialog
	@Override
	protected void drawImpl() {
		// Darken the screen
		noStroke();
		fill(transparentColour, transparentAlpha);
		rectMode(CORNER);
		rect(0, 0, width, height);

		// Border
		stroke(borderColour);
		strokeWeight(2);
		fill(transparentColour, transparentAlpha);
		rect(outerPadding, outerPadding, dialogWidth, dialogHeight, 30);

		// Dialog background
		noStroke();
		fill(transparentColour, transparentAlpha);
		rect(outerPadding + innerPadding, outerPadding + innerPadding, dialogWidth - innerPadding * 2, dialogHeight - innerPadding * 2);

		// Header background
		fill(headerColour);
		rect(outerPadding + innerPadding, outerPadding + innerPadding, dialogWidth - innerPadding * 2, headerSize + 5);

		// Header text
		fill(textColour);
		textFont(headerFont);
		textAlign(CENTER, CENTER);
		text(headerText, outerPadding + innerPadding, outerPadding + innerPadding, dialogWidth - innerPadding * 2, headerSize);
	}

	// Draw for zone picker
	@Override
	protected void pickDrawImpl() {
		rect(0, 0, width, height);
	}

	// Action on the touch event
	@Override
	protected void touchImpl() {
		Application.setActionPerformed();
	}

	// Retrieve the list of files from the specified folder and filter them by
	// extension if it is specified
	protected void getFiles() {
		LOGGER.info("FileBrowser is retrieving files in the specified folder.");

		currentRow = 0;
		currentColumn = 0;
		currentPage = 0;
		itemWidth = ((float) dialogWidth - innerPadding * 2) / maxColumns;
		itemHeight = ((float) dialogHeight - innerPadding * 2 - (headerSize + 5)) / maxRows;

		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();
		files = new ArrayList<File>();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if (extension != null && extension.length() > 0) {
					if (listOfFiles[i].getName().toLowerCase().endsWith(extension))
						files.add(listOfFiles[i]);
				} else {
					files.add(listOfFiles[i]);
				}
			}
		}

		maxPages = (int) Math.ceil((float) files.size() / itemsPerPage);

		// Sort files from newest to oldest if dealing with saves
		if (SaveFile.SAVE_FILE_EXT.equals(extension)) {
			Collections.reverse(files);
		}
	}

	/**
	 * Close the dialog. This method may be called by the close button.
	 * 
	 * @see CloseButton
	 */
	public void close() {
		LOGGER.info("FileBrowser closed.");
		TouchClient.remove(this);
		Application.resumeApplication();
		isOnScreen = false;
	}

	/**
	 * Show the next page of items.
	 */
	public void nextPage() {
		currentPage++;
		clearPage();
		showPage();
	}

	/**
	 * Show the previous page of items.
	 */
	public void prevPage() {
		currentPage--;
		clearPage();
		showPage();
	}

	// Remove all file items from the dialog
	protected void clearPage() {
		remove(leftArrow);
		remove(rightArrow);

		Zone[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			Zone child = children[i];
			if (child instanceof FileButton)
				remove(child);
		}
	}

	// Show all file items depending on the selected page
	protected void showPage() {
		int i = currentPage * itemsPerPage;
		currentColumn = 0;
		currentRow = 0;

		while (i < files.size() && i < (currentPage + 1) * itemsPerPage)
			showFile(files.get(i++));

		// Show navigation arrows if needed
		if (currentPage > 0)
			add(leftArrow);
		if (currentPage < maxPages - 1)
			add(rightArrow);
	}

	// Display a single file item
	protected void showFile(File file) {
		String fileName = file.getName();
		String fileExt = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();

		int fileX = (int) (innerPadding + outerPadding + currentColumn * itemWidth);
		int fileY = (int) (innerPadding + outerPadding + (headerSize + 5) + currentRow * itemHeight);

		FileButton fb = new FileButton(fileX, fileY);
		fb.fileName = fileName.substring(0, fileName.lastIndexOf('.')).replaceAll("_", " ");
		fb.filePath = file.getPath();

		if (SaveFile.SAVE_FILE_EXT.equals(fileExt))
			fb.fileType = FILE_TYPE_SAVE;
		else if (".png".equals(fileExt) || ".jpg".equals(fileExt) || ".gif".equals(fileExt) || ".tga".equals(fileExt))
			fb.fileType = FILE_TYPE_IMAGE;
		else
			fb.fileType = FILE_TYPE_OTHER;

		fb.initialize();
		add(fb);

		currentColumn++;
		if (currentColumn >= maxColumns) {
			currentColumn = 0;
			currentRow++;
		}
	}

	/**
	 * This class represents a single item in the list of files in the file
	 * browser. Image files are displayed as thumbnails, save files generated by
	 * ePad application have thumbnails as well, all other files are displayed
	 * as text (path to the file).
	 * 
	 * @author Dmitry Pyryeskin
	 * @version 1.0
	 * 
	 */
	public class FileButton extends Zone {
		/**
		 * Name of the file (without extension).
		 */
		public String fileName;
		/**
		 * Path to the file.
		 */
		public String filePath;
		/**
		 * Type of the file (FILE_TYPE_IMAGE, FILE_TYPE_SAVE, FILE_TYPE_OTHER).
		 */
		public int fileType;
		/**
		 * SaveFile object associated with the file.
		 */
		public SaveFile save;

		// Item's cache to speed up drawing
		private PImage cache;
		// Button down flag
		protected boolean buttonDown = false;
		// Thumbnail image
		protected PImage img;

		/**
		 * Default constructor.
		 * 
		 * @param x
		 *            x-coordinate of the top left corner of the FileButton
		 * @param y
		 *            y-coordinate of the top left corner of the FileButton
		 */
		public FileButton(int x, int y) {
			super(x, y, (int) itemWidth, (int) itemHeight);
		}

		/**
		 * Initialise the item: load the image, if the file is an image file,
		 * load thumbnail if it is a save file and generate cache.
		 */
		protected void initialize() {
			if (fileType == FILE_TYPE_IMAGE) {
				// Load image
				img = applet.loadImage(filePath);
				if (img == null)
					LOGGER.error("Failed to load image: " + filePath);
			} else if (fileType == FILE_TYPE_SAVE) {
				// Load thumbnail
				save = new SaveFile();
				save.load(filePath);

				img = applet.loadImage(save.thumbnailPath);
				if (img == null)
					LOGGER.error("Failed to load image: " + save.thumbnailPath);
			}

			// Create a cached image
			PGraphics tempG = applet.createGraphics((int) itemWidth, (int) itemHeight, PConstants.JAVA2D);
			tempG.beginDraw();
			tempG.smooth();
			tempG.background(0);

			tempG.stroke(Application.secondaryColour);
			tempG.strokeWeight(1);
			tempG.fill(Application.primaryColour);
			tempG.rect(0, 0, itemWidth, itemHeight);

			if (img != null) {
				if (fileType == FILE_TYPE_IMAGE) {
					setImage(tempG, 0, itemHeaderSize + 10, width - 20, height - itemHeaderSize - 40, true);
				} else if (fileType == FILE_TYPE_SAVE) {
					setImage(tempG, 0, itemHeaderSize + 40, width - 20, height - itemHeaderSize - 60, true);
				}
			}

			tempG.fill(textColour);
			tempG.textFont(itemFont);
			if (fileType == FILE_TYPE_IMAGE) {
				tempG.text(fileName, 10, itemHeaderSize);
			} else if (fileType == FILE_TYPE_SAVE) {
				tempG.text("Name: " + save.userName, 10, itemHeaderSize);
				tempG.text("Date : " + DATE_FORMAT.format(save.saveTime), 10, itemHeaderSize * 2);
			} else {
				tempG.text(filePath, 10, itemHeaderSize);
			}

			tempG.endDraw();
			cache = tempG.get();
		}

		// Draw the item
		@Override
		protected void drawImpl() {
			image(cache, 0, 0);
		}

		// Draw for zone picker
		@Override
		protected void pickDrawImpl() {
			rect(0, 0, itemWidth, itemHeight);
		}

		// Action on the touch event
		@Override
		protected void touchImpl() {
			Application.setActionPerformed();
		}

		// Action on the touch up event
		@Override
		protected void touchUp(Touch touch) {
			buttonDown = getTouches().length > 0;
			super.touchUp(touch);

			if (buttonDown) {
				close();
				notifyListeners(this, FILE_SELECTED);
			}
			buttonDown = false;
		}

		// Action on the touch down event
		@Override
		protected void touchDown(Touch touch) {
			super.touchDown(touch);
			buttonDown = true;
		}

		// Resize the image according to the size of the FileButton
		private void setImage(PGraphics g, int x, int y, int maxWidth, int maxHeight, boolean showBorder) {
			// TODO: this method doesn't work with certain aspect ratios, must be fixed
			float bgX = x;
			float bgY = y;
			float bgWidth = maxWidth;
			float bgHeight = maxHeight;

			if (bgHeight == 0 || img.height == 0 || img.width == 0) {
				return;
			}

			float aspectBoard = bgWidth / bgHeight;
			float aspectImage = (float) img.width / img.height;
			if (aspectBoard < aspectImage) {
				bgHeight = bgWidth / aspectImage;
				bgY += (height - bgHeight) / 2;
			} else if (aspectBoard > aspectImage) {
				bgWidth = bgHeight * aspectImage;
				bgX += (width - bgWidth) / 2;
			}

			if (showBorder)
				g.stroke(Application.secondaryColour);
			else
				g.noStroke();
			g.fill(Application.backgroundColour);
			g.rect(bgX, bgY, bgWidth, bgHeight);

			g.image(img, bgX, bgY, bgWidth, bgHeight);
		}
	}

	// Send a message to all listeners
	private void notifyListeners(Object source, String message) {
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
	public void addListener(ActionListener listener) {
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
	public boolean removeListener(ActionListener listener) {
		return listeners.remove(listener);
	}

	/**
	 * 
	 * @return <b>true</b> if the dialog is currently displayed on screen and
	 *         <b>false</b> otherwise.
	 */
	public static boolean isOnScreen() {
		return isOnScreen;
	}
}
