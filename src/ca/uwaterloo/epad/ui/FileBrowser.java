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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import processing.core.PFont;
import processing.core.PImage;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.xml.SaveFile;

public class FileBrowser extends Zone {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd yyyy, hh:mm aaa");
	
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

	public FileBrowser(String header, String folder, String extension, int columns, int rows) {
		super(0, 0, applet.width, applet.height);

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
		leftArrow.setPressMethodByName("prevPage", this);
		rightArrow = new IconButton(applet.width - outerPadding - 25, applet.height/2 - 75/2, 75, 75, "arrow_right", borderColour, backgroundColour);
		rightArrow.setPressMethodByName("nextPage", this);
		
		getFiles();
		showPage();
		
		add(new CloseButton(browserWidth + outerPadding - 50, outerPadding - 25, 75, 75));
	}
	
	public FileBrowser(String header, String folder, String extension) {
		this(header, folder, extension, 5, 3);
	}

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

	protected void pickDrawImpl() {
		rect(0, 0, width, height);
	}
	
	protected void touchImpl() {
		Application.setActionPerformed();
	}
	
	public void close() {
		TouchClient.remove(this);
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
		
		switch (fileExt) {
		case SaveFile.SAVE_FILE_EXT: fb.fileType = FileButton.TYPE_SAVE; break;
		case ".png":
		case ".jpg":
		case ".jpeg": fb.fileType = FileButton.TYPE_IMAGE; break;
		default: fb.fileType = FileButton.TYPE_OTHER;
		}
		
		fb.initialize();
		add(fb);
		
		currentColumn ++;
		if (currentColumn >= maxColumns) {
			currentColumn = 0;
			currentRow ++;
		}
	}
	
	private class FileButton extends Zone {
		public static final int TYPE_IMAGE = 0;
		public static final int TYPE_SAVE = 1;
		public static final int TYPE_OTHER = 2;
		
		public String fileName;
		public String filePath;
		public int fileType;
		public SaveFile save;
		
		protected boolean buttonDown = false;
		protected PImage img;
		
		public FileButton(int x, int y) {
			super(x, y, (int)itemWidth, (int)itemHeight);
		}
		
		protected void initialize() {
			if (fileType == TYPE_IMAGE) {
				img = applet.loadImage(filePath);
			} else if (fileType == TYPE_SAVE) {
				save = new SaveFile();
				save.load(filePath);
				img = applet.loadImage(save.thumbnailPath);
			}
		}
		
		protected void drawImpl() {
			stroke(Application.secondaryColour);
			strokeWeight(1);
			fill(Application.primaryColour);
			rect(0, 0, itemWidth, itemHeight);
			
			if (img != null) {
				if (fileType == TYPE_IMAGE)
					showImage(10, itemHeaderSize + 10, width-20, height-itemHeaderSize-40);
				else if (fileType == TYPE_SAVE)
					showImage(10, itemHeaderSize + 40, width-20, height-itemHeaderSize-60);
			}
			
			fill(textColour);
			textFont(itemFont);
			if (fileType == TYPE_IMAGE) {
				text(fileName, 10, itemHeaderSize);
			} else if (fileType == TYPE_SAVE) {
				text("Name: " + save.userName, 10, itemHeaderSize);
				text("Date : " + DATE_FORMAT.format(save.saveTime), 10, itemHeaderSize*2);
			}
		}
		
		protected void pickDrawImpl() {
			rect(0, 0, itemWidth, itemHeight);
		}
		
		public void touchImpl() {
			Application.setActionPerformed();
		}
		
		public void touchUp(Touch touch) {
			setButtonDown();
			super.touchUp(touch);

			if (buttonDown) {
				// TODO: use proper action on button press
				if (fileType == TYPE_IMAGE) {
					Application.getCanvas().setOverlayImage(filePath);
					close();
				} else if (fileType == TYPE_SAVE) {
					Application.loadSave(save);
				}
			}
			buttonDown = false;
		}
		
		public void touchDown(Touch touch) {
			super.touchDown(touch);
			buttonDown = true;
		}
		
		protected boolean setButtonDown() {
			buttonDown = getTouches().length > 0;
			return buttonDown;
		}
		
		private void showImage(int x, int y, int maxWidth, int maxHeight) {
			float bgX = x;
			float bgY = y;
			float bgWidth = maxWidth;
			float bgHeight = maxHeight;

			if (bgHeight == 0 || img.height == 0 || img.width == 0) {
				return;
			}

			float aspectBoard = (float) bgWidth / bgHeight;
			float aspectImage = (float) img.width / img.height;
			if (aspectBoard < aspectImage) {
				bgHeight = bgWidth / aspectImage;
				bgY += (height - bgHeight) / 2;
			} else if (aspectBoard > aspectImage) {
				bgWidth = bgHeight * aspectImage;
				bgX += (width - bgWidth) / 2;
			}

			image(img, bgX, bgY, bgWidth, bgHeight);
		}
	}
}
