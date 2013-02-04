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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import processing.core.PImage;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;

/**
 * This class represents a save file created by ePad application and also
 * handles saving the workspace and loading the save files.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class SaveFile {
	private static final Logger LOGGER = Logger.getLogger(SaveFile.class);

	// String keys
	public static final String USER_NAME = "user_name";
	public static final String SAVE_TIME = "time";
	public static final String DIRECTORY = "data_dir";
	public static final String THUMBNAIL = "thumbnailPath";
	public static final String DRAWING = "drawingPath";
	public static final String LAYOUT = "layoutPath";

	/**
	 * Default extension of the save files.
	 */
	public static final String SAVE_FILE_EXT = ".sav";
	/**
	 * Default suffix of the directories paired with the save files.
	 */
	public static final String DIRECTORY_SUFFIX = "_data";

	// Path to the save folder
	private static final String SAVE_FOLDER = Settings.saveFolder;
	// Date formatter
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");
	// Maximum dimension of a thumbnail in pixels
	private static final int THUMBNAIL_MAX_SIZE = 300;

	// Number of parameters that have to be loaded from the save file (name,
	// date, directory, thumbnail, drawing and layout)
	private static final int PARAMETER_COUNT = 6;

	/**
	 * Name of the save file.
	 */
	public String filename;
	/**
	 * Name of the data directory paired with the save file.
	 */
	public String dirname;
	/**
	 * Name of the user.
	 */
	public String userName;
	/**
	 * Time when the save file was created.
	 */
	public Date saveTime;
	/**
	 * Thumbnail image.
	 */
	public PImage thumbnail;
	/**
	 * Drawing image.
	 */
	public PImage drawing;
	/**
	 * Path to the thumbnail image.
	 */
	public String thumbnailPath;
	/**
	 * Path to the drawing image.
	 */
	public String drawingPath;
	/**
	 * Path to the saved layout.
	 */
	public String layoutPath;

	/**
	 * Create a save file for the specified user and save the current drawing
	 * and workspace layout.
	 * 
	 * @param userName
	 *            name of the user
	 * @return <b>true</b> if the file was saved successfully and <b>false</b>
	 *         if something went wrong
	 */
	public boolean save(String userName) {
		// Get time stamp
		saveTime = new Date();
		String timestamp = DATE_FORMAT.format(saveTime);

		// Create file and directory names
		filename = SAVE_FOLDER + timestamp + SAVE_FILE_EXT;
		dirname = SAVE_FOLDER + timestamp + DIRECTORY_SUFFIX;
		thumbnailPath = dirname + "\\thumbnail.png";
		drawingPath = dirname + "\\drawing.png";
		layoutPath = dirname + "\\layout.xml";

		LOGGER.info("Saving the workspace into file: " + filename);

		// Check if the save folder exists and create it if necessary
		try {
			File savedir = new File(SAVE_FOLDER);
			if (!savedir.exists())
				savedir.mkdir();

			LOGGER.info("Created a save directory: " + SAVE_FOLDER);
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			return false;
		}

		try {
			// Write the data into the save file
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

		// Get drawing and thumbnail images from the canvas
		drawing = Application.getCanvas().getDrawing(true);
		thumbnail = Application.getCanvas().getDrawing(false);

		// Resize the thumbnail while preserving the aspect ratio
		int w, h;
		if (thumbnail.width >= thumbnail.height) {
			w = THUMBNAIL_MAX_SIZE;
			h = Math.round((float) thumbnail.height * (float) THUMBNAIL_MAX_SIZE / thumbnail.width);
		} else {
			h = THUMBNAIL_MAX_SIZE;
			w = Math.round((float) thumbnail.width * (float) THUMBNAIL_MAX_SIZE / thumbnail.height);
		}
		thumbnail.resize(w, h);

		// Save thumbnail
		if (thumbnail.save(thumbnailPath))
			LOGGER.info("Thumbnail saved: " + thumbnailPath);
		else
			LOGGER.error("Failed to save Thumbnail");

		// Save drawing
		if (drawing.save(drawingPath))
			LOGGER.info("Drawing saved: " + drawingPath);
		else
			LOGGER.error("Failed to save drawing");

		// Save layout
		try {
			SimpleMarshaller.marshallLayout(new File(layoutPath));
			LOGGER.info("Layout saved: " + layoutPath);
		} catch (Exception e) {
			LOGGER.error("Failed to save layout");
		}

		return true;
	}

	/**
	 * Load the specified save file and retrieve the recorded data such as
	 * user's name, time stamp as well as the paths to the thumbnail, drawing
	 * and layout files.
	 * 
	 * @param filename
	 *            valid path to a save file to load
	 * @return <b>true</b> if the file was loaded and parsed successfully and
	 *         <b>false</b> if something went wrong
	 */
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
					userName = line.substring(USER_NAME.length() + 1);
					lineCount++;
				} else if (line.startsWith(SAVE_TIME)) {
					saveTime = DATE_FORMAT.parse(line.substring(SAVE_TIME.length() + 1));
					lineCount++;
				} else if (line.startsWith(DIRECTORY)) {
					dirname = line.substring(DIRECTORY.length() + 1);
					lineCount++;
				} else if (line.startsWith(THUMBNAIL)) {
					thumbnailPath = line.substring(THUMBNAIL.length() + 1);
					lineCount++;
				} else if (line.startsWith(DRAWING)) {
					drawingPath = line.substring(DRAWING.length() + 1);
					lineCount++;
				} else if (line.startsWith(LAYOUT)) {
					layoutPath = line.substring(LAYOUT.length() + 1);
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
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			return false;
		}

		return true;
	}
}
