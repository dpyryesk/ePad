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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import processing.core.PImage;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;

public class SaveFile {
	private static final Logger LOGGER = Logger.getLogger(SaveFile.class);
	
	public static final String USER_NAME = "user_name";
	public static final String SAVE_TIME = "time";
	public static final String DIRECTORY = "data_dir";
	public static final String THUMBNAIL = "thumbnailPath";
	public static final String DRAWING = "drawingPath";
	public static final String LAYOUT= "layoutPath";
	
	public static final String SAVE_FILE_EXT = ".sav";
	public static final String DIRECTORY_SUFFIX = "_data";
	
	private static final String SAVE_FOLDER = Settings.saveFolder;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");
	private static final int THUMBNAIL_MAX_SIZE = 300;
	private static final int PARAMETER_COUNT = 6; // number of parameters that have to be loaded from the save file (name, date, directory, thumbnail, drawing and layout)

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

		LOGGER.info("Saving the workspace into file: " + filename);
		
		try {
			File file = new File(filename);
			File dir = new File(dirname);
			if (file.exists()) {
				LOGGER.error("Save file already exists " + filename);
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
			out.close();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			return false;
		}
		
		drawing = Application.getCanvas().getDrawing(true);
		thumbnail = Application.getCanvas().getDrawing(false);
		int w, h;
		if (thumbnail.width >= thumbnail.height) {
			w = THUMBNAIL_MAX_SIZE;
			h = Math.round((float)thumbnail.height * (float)THUMBNAIL_MAX_SIZE / (float)thumbnail.width);
		} else {
			h = THUMBNAIL_MAX_SIZE;
			w = Math.round((float)thumbnail.width * (float)THUMBNAIL_MAX_SIZE / (float)thumbnail.height);
		}
		thumbnail.resize(w, h);
		
		if (thumbnail.save(thumbnailPath))
			LOGGER.info("Thumbnail saved: " + thumbnailPath);
		else
			LOGGER.error("Failed to save Thumbnail");
		
		if (drawing.save(drawingPath))
			LOGGER.info("Drawing saved: " + drawingPath);
		else
			LOGGER.error("Failed to save drawing");
		
		try {
			SimpleMarshaller.marshallLayout(new File(layoutPath));
			LOGGER.info("Layout saved: " + layoutPath);
		} catch (Exception e) {
			LOGGER.error("Failed to save layout");
		}

		return true;
	}

	public boolean load(String filename) {
		this.filename = filename;
		File file = new File(filename);
		if (!file.exists())
			return false;
		
		try {
			FileReader fr = new FileReader(file);
			BufferedReader in = new BufferedReader(fr);
			
			String line = in.readLine();
			int lineCount = 0;
			while (line != null) {
				if (line.startsWith(USER_NAME)) {
					userName = line.substring(USER_NAME.length()+1);
					lineCount++;
				} else if (line.startsWith(SAVE_TIME)) {
					saveTime = DATE_FORMAT.parse(line.substring(SAVE_TIME.length()+1));
					lineCount++;
				} else if (line.startsWith(DIRECTORY)) {
					dirname = line.substring(DIRECTORY.length()+1);
					lineCount++;
				} else if (line.startsWith(THUMBNAIL)) {
					thumbnailPath = line.substring(THUMBNAIL.length()+1);
					lineCount++;
				} else if (line.startsWith(DRAWING)) {
					drawingPath = line.substring(DRAWING.length()+1);
					lineCount++;
				} else if (line.startsWith(LAYOUT)) {
					layoutPath = line.substring(LAYOUT.length()+1);
					lineCount++;
				}
				
				line = in.readLine();
			}
			
			in.close();
			
			// check if all parameters were loaded
			if (lineCount != PARAMETER_COUNT) {
				LOGGER.error("Save file " + filename + " contains fewer parameters than expected.");
				return false;
			}
		} catch (IOException | ParseException e) {
			LOGGER.error(e.getLocalizedMessage());
			return false;
		}
		
		return true;
	}
}
