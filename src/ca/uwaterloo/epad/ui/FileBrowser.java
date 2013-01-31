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

public class FileBrowser extends Zone {
	private static final Logger LOGGER = Logger.getLogger(FileBrowser.class);
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd yyyy, hh:mm aaa");
	
	public static final String FILE_SELECTED = "file selected";
	public static final int FILE_TYPE_IMAGE = 0;
	public static final int FILE_TYPE_SAVE = 1;
	public static final int FILE_TYPE_OTHER = 2;
	
	public int backgroundColour = Application.backgroundColour;
	public int borderColour = Application.primaryColour;
	public int transparentColour = Application.transparentColour;
	public int transparentAlpha = Application.transparentAlpha;
	public int textColour = Application.textColour;
	public int headerColour = Application.secondaryColour;

	private int outerPadding = 70;
	private int innerPadding = 30;
	private int headerSize = 50;
	private int itemHeaderSize = 20;
	private int browserWidth, browserHeight;
	
	private int currentRow, currentColumn, currentPage;
	private int maxColumns;
	private int maxRows ;
	private int maxPages;
	private int itemsPerPage;
	private float itemWidth;
	private float itemHeight;

	private String headerText;
	private String folderPath;
	private String extension;
	private ArrayList<File> files;
	private static PFont headerFont, itemFont;
	private IconButton leftArrow, rightArrow;
	
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	
	private static boolean isOnScreen;

	public FileBrowser(String header, String folder, String extension, int columns, int rows) {
		super(0, 0, applet.width, applet.height);
		
		LOGGER.info("FileBrowser opened: folder=" + folder + ", extension=" + extension);
		
		Application.pauseApplication();
		isOnScreen = true;

		browserWidth = applet.width - outerPadding * 2;
		browserHeight = applet.height - outerPadding * 2;

		this.folderPath = folder;
		this.extension = extension;
		this.headerText = header;
		this.maxColumns = columns;
		this.maxRows = rows;
		itemsPerPage = maxColumns * maxRows;
		
		headerFont = applet.createFont("Arial", headerSize);
		itemFont = applet.createFont("Arial", itemHeaderSize);
		leftArrow = new IconButton(outerPadding - 50, applet.height/2 - 75/2, 75, 75, "arrow_left", borderColour, backgroundColour);
		leftArrow.setPressMethod("prevPage", this);
		rightArrow = new IconButton(applet.width - outerPadding - 25, applet.height/2 - 75/2, 75, 75, "arrow_right", borderColour, backgroundColour);
		rightArrow.setPressMethod("nextPage", this);
		
		getFiles();
		showPage();
		
		add(new CloseButton(browserWidth + outerPadding - 50, outerPadding - 25, 75, 75));
	}
	
	public FileBrowser(String header, String folder, String extension) {
		this(header, folder, extension, 5, 3);
	}

	@Override
	protected void drawImpl() {
		noStroke();
		fill(transparentColour, transparentAlpha);
		rectMode(CORNER);
		rect(0, 0, width, height);

		stroke(borderColour);
		strokeWeight(2);
		fill(transparentColour, transparentAlpha);
		rect(outerPadding, outerPadding, browserWidth, browserHeight, 30);

		noStroke();
		fill(transparentColour, transparentAlpha);
		rect(outerPadding + innerPadding, outerPadding + innerPadding, browserWidth - innerPadding * 2, browserHeight - innerPadding * 2);
		
		fill(headerColour);
		rect(outerPadding + innerPadding, outerPadding + innerPadding, browserWidth - innerPadding * 2, headerSize + 5);
		
		fill(textColour);
		textFont(headerFont);
		textAlign(CENTER, CENTER);
		text(headerText, outerPadding + innerPadding, outerPadding + innerPadding, browserWidth - innerPadding * 2, headerSize);
	}

	@Override
	protected void pickDrawImpl() {
		rect(0, 0, width, height);
	}
	
	@Override
	protected void touchImpl() {
		Application.setActionPerformed();
	}
	
	public void close() {
		LOGGER.info("FileBrowser closed.");
		TouchClient.remove(this);
		Application.resumeApplication();
		isOnScreen = false;
	}
	
	public void nextPage() {
		currentPage++;
		clearPage();
		showPage();
	}
	
	public void prevPage() {
		currentPage--;
		clearPage();
		showPage();
	}

	protected void getFiles() {
		LOGGER.info("FileBrowser is retrieving files in the specified folder.");
		
		currentRow = 0;
		currentColumn = 0;
		currentPage = 0;
		itemWidth = ((float)browserWidth - innerPadding * 2) / maxColumns;
		itemHeight = ((float)browserHeight - innerPadding * 2 - (headerSize + 5)) / maxRows;
		
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
		
		// sort files from newest to oldest if dealing with saves
		if (SaveFile.SAVE_FILE_EXT.equals(extension)) {
			Collections.reverse(files);
		}
	}
	
	private void clearPage() {
		remove(leftArrow);
		remove(rightArrow);
		
		Zone[] children = getChildren();
		for (int i=0; i < children.length; i++) {
			Zone child = children[i];
			if (child instanceof FileButton)
				remove(child);
		}
	}
	
	private void showPage() {
		int i = currentPage * itemsPerPage;
		currentColumn = 0;
		currentRow = 0;
		
		while (i < files.size() && i < (currentPage + 1) * itemsPerPage)
			showFile(files.get(i++));
		
		// show navigation arrows if needed
		if (currentPage > 0)
			add(leftArrow);
		if (currentPage < maxPages-1)
			add(rightArrow);
	}
	
	private void showFile(File file) {
		String fileName = file.getName();
		String fileExt = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
		
		int fileX = (int) (innerPadding + outerPadding + currentColumn * itemWidth);
		int fileY = (int) (innerPadding + outerPadding + (headerSize + 5) + currentRow * itemHeight);
		
		FileButton fb = new FileButton(fileX, fileY);
		fb.fileName = fileName.substring(0, fileName.lastIndexOf('.')).replaceAll("_", " ");
		fb.filePath = file.getPath();
		
		if (SaveFile.SAVE_FILE_EXT.equals(fileExt)) fb.fileType = FILE_TYPE_SAVE;
		else if (".png".equals(fileExt) || ".jpg".equals(fileExt) || ".jpeg".equals(fileExt)) fb.fileType = FILE_TYPE_IMAGE;
		else fb.fileType = FILE_TYPE_OTHER;
		
		fb.initialize();
		add(fb);
		
		currentColumn ++;
		if (currentColumn >= maxColumns) {
			currentColumn = 0;
			currentRow ++;
		}
	}
	
	public class FileButton extends Zone {
		public String fileName;
		public String filePath;
		public int fileType;
		public SaveFile save;
		
		private PImage cache;
		
		protected boolean buttonDown = false;
		protected PImage img;
		
		public FileButton(int x, int y) {
			super(x, y, (int)itemWidth, (int)itemHeight);
		}
		
		protected void initialize() {
			if (fileType == FILE_TYPE_IMAGE) {
				img = applet.loadImage(filePath);
				if (img == null)
					LOGGER.error("Failed to load image: " + filePath);
			} else if (fileType == FILE_TYPE_SAVE) {
				save = new SaveFile();
				save.load(filePath);
				
				img = applet.loadImage(save.thumbnailPath);
				if (img == null)
					LOGGER.error("Failed to load image: " + save.thumbnailPath);
			}
			
			// create a cached image
			PGraphics tempG = applet.createGraphics((int)itemWidth, (int)itemHeight, PConstants.JAVA2D);
			tempG.beginDraw();
			tempG.smooth();
			tempG.background(0);
			
			tempG.stroke(Application.secondaryColour);
			tempG.strokeWeight(1);
			tempG.fill(Application.primaryColour);
			tempG.rect(0, 0, itemWidth, itemHeight);
			
			if (img != null) {
				if (fileType == FILE_TYPE_IMAGE) {
					showImage(tempG, 0, itemHeaderSize + 10, width-20, height-itemHeaderSize-40, true);
				} else if (fileType == FILE_TYPE_SAVE) {
					showImage(tempG, 0, itemHeaderSize + 40, width-20, height-itemHeaderSize-60, true);
				}
			}
			
			tempG.fill(textColour);
			tempG.textFont(itemFont);
			if (fileType == FILE_TYPE_IMAGE) {
				tempG.text(fileName, 10, itemHeaderSize);
			} else if (fileType == FILE_TYPE_SAVE) {
				tempG.text("Name: " + save.userName, 10, itemHeaderSize);
				tempG.text("Date : " + DATE_FORMAT.format(save.saveTime), 10, itemHeaderSize*2);
			} else {
				tempG.text(filePath, 10, itemHeaderSize);
			}
			
			tempG.endDraw();
			cache = tempG.get();
		}
		
		@Override
		protected void drawImpl() {
			image(cache, 0, 0);
		}
		
		@Override
		protected void pickDrawImpl() {
			rect(0, 0, itemWidth, itemHeight);
		}
		
		@Override
		public void touchImpl() {
			Application.setActionPerformed();
		}
		
		@Override
		public void touchUp(Touch touch) {
			setButtonDown();
			super.touchUp(touch);

			if (buttonDown) {
				close();
				notifyListeners(this, FILE_SELECTED);
			}
			buttonDown = false;
		}
		
		@Override
		public void touchDown(Touch touch) {
			super.touchDown(touch);
			buttonDown = true;
		}
		
		protected boolean setButtonDown() {
			buttonDown = getTouches().length > 0;
			return buttonDown;
		}
		
		private void showImage(PGraphics g, int x, int y, int maxWidth, int maxHeight, boolean showBorder) {
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
	
	private void notifyListeners(Object source, String message) {
		for (int i = 0; i < listeners.size(); i++) {
			ActionListener listener = listeners.get(i);
			if (listener != null)
				listener.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_FIRST, message));
			else
				LOGGER.error("A listener is null: " + i);
		}
	}

	public void addListener(ActionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public boolean removeListener(ActionListener listener) {
		return listeners.remove(listener);
	}
	
	public static boolean IsOnScreen() {
		return isOnScreen;
	}
}
