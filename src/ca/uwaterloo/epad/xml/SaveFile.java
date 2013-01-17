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

package ca.uwaterloo.epad.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import processing.core.PImage;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;

public class SaveFile {
	public static final String USER_NAME = "user_name";
	public static final String SAVE_TIME = "time";
	public static final String DIRECTORY = "data_dir";
	public static final String THUMBNAIL = "thumbnailPath";
	public static final String DRAWING = "drawingPath";
	public static final String LAYOUT= "layoutPath";
	public static final String OVERLAY= "overlay";
	
	public static final String SAVE_FILE_EXT = ".sav";
	public static final String DIRECTORY_SUFFIX = "_data";
	
	private static final String SAVE_FOLDER = Settings.saveFolder;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");
	private static final int THUMBNAIL_MAX_SIZE = 150;

	public String filename;
	public String dirname;
	public String userName;
	public Date saveTime;
	public PImage thumbnail, drawing;
	public String thumbnailPath, drawingPath, layoutPath;

	public boolean save(String userName) {
		saveTime = new Date();
		String timestamp = DATE_FORMAT.format(saveTime);
		filename = SAVE_FOLDER + timestamp + SAVE_FILE_EXT;
		dirname = SAVE_FOLDER + timestamp + DIRECTORY_SUFFIX;
		thumbnailPath = dirname + "\\thumbnail.png";
		drawingPath = dirname + "\\drawing.png";
		layoutPath = dirname + "\\layout.xml";

		try {
			File file = new File(filename);
			File dir = new File(dirname);
			if (file.exists()) {
				System.err.println("Save file already exists " + filename);
				return false;
			}
			
			dir.mkdir();
			
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			out.write(USER_NAME + "=" + userName + "\n");
			out.write(SAVE_TIME + "=" + DATE_FORMAT.format(saveTime) + "\n");
			out.write(DIRECTORY + "=" + dirname + "\n");
			out.write(THUMBNAIL + "=" + thumbnailPath + "\n");
			out.write(DRAWING + "=" + drawingPath + "\n");
			out.write(LAYOUT + "=" + layoutPath + "\n");
			out.write(OVERLAY + "=" + Application.getCanvas().getOverlayImagePath() + "\n");
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			return false;
		}
		
		drawing = Application.getCanvas().getDrawing(true);
		thumbnail = Application.getCanvas().getDrawing(false); // Application.getLayoutScreenshot();
		int w = THUMBNAIL_MAX_SIZE;
		int h = Math.round((float)thumbnail.height * (float)THUMBNAIL_MAX_SIZE / (float)thumbnail.width);
		thumbnail.resize(w, h);
		
		if (thumbnail.save(thumbnailPath))
			System.out.println("Thumbnail saved: " + thumbnailPath);
		else
			System.err.println("Failed to save Thumbnail");
		
		if (drawing.save(drawingPath))
			System.out.println("Drawing saved: " + drawingPath);
		else
			System.err.println("Failed to save drawing");
		
		try {
			SimpleMarshaller.marshallLayout(new File(layoutPath));
			System.out.println("Layout saved: " + layoutPath);
		} catch (Exception e) {
			System.err.println("Failed to save layout");
		}

		return true;
	}

	public boolean load(String filename) {
		return false;
	}
}
