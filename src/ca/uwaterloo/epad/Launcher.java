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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import processing.core.PApplet;
import ca.uwaterloo.epad.ui.SplashScreen;
import ca.uwaterloo.epad.util.Settings;

/**
 * This class is the entry point of ePad 2.0 application. It loads the settings
 * from an xml file, sets up logging, shows the splash screen and starts the
 * Application class in full-screen mode.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class Launcher {
	private static ResourceBundle uiStrings;
	private static final Logger LOGGER = Logger.getLogger(Launcher.class);

	private static String settingsFile = "data\\settings.xml";
	private static String logConfigFile = "data\\log4j.properties";

	/**
	 * Main method reads the arguments as described below:</br> If no arguments
	 * are specified, the application will attempt to load the settings file and
	 * the logger properties file from the default locations:
	 * <i>data\\settings.xml</i> and <i>data\\log4j.properties</i>
	 * respectively.</br> If one argument is specified, it will be interpreted
	 * as the location of the settings file.</br> Finally, if two arguments are
	 * specified, the first one will be interpreted as the location of the
	 * setting file and the second - as the location of the logger properties
	 * file.</br> The third and further arguments will be ignored.
	 * 
	 * @param args
	 *            command line arguments
	 * 
	 */
	public static void main(String args[]) {
		// Read arguments
		if (args.length == 1) {
			settingsFile = args[0];
		} else if (args.length >= 2) {
			settingsFile = args[0];
			logConfigFile = args[1];
		}

		// Set up logging
		PropertyConfigurator.configure(logConfigFile);

		// Load settings from xml file
		try {
			Settings.unmarshallSettings(settingsFile);
		} catch (Exception e) {
			LOGGER.fatal("Failed to load settings from file: " + settingsFile);
			System.exit(1);
		}

		// Make sure that the resource bundles can be loaded
		try {
			uiStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.UI", Settings.locale);
			ResourceBundle.getBundle("ca.uwaterloo.epad.res.Prompts", Settings.locale);
		} catch (MissingResourceException e) {
			LOGGER.fatal(e.getLocalizedMessage());
			System.exit(2);
		}

		// Create a splash screen
		SplashScreen.splash(Settings.dataFolder + "images\\epadLogo.png");
		SplashScreen.setMessage(uiStrings.getString("StartingMessage"));

		// Mark the starting point in the log
		LOGGER.info("=======================================================================================");
		LOGGER.info("Starting the application.");

		// Start the applet in full-screen mode
		args = new String[] { "--location=0,0", "--full-screen", "--hide-stop" };
		PApplet.main("ca.uwaterloo.epad.Application", args);
	}
}