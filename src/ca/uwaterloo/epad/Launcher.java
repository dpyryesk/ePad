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

public class Launcher {
	private static ResourceBundle uiStrings;
	private static final Logger LOGGER = Logger.getLogger(Launcher.class);
	
	private static String settingsFile = "data\\settings.xml";
	private static String logConfigFile = "data\\log4j.properties";
	
	public static void main(String args[]) {
		if (args.length == 1) {
			settingsFile = args[0];
		} else if (args.length == 2) {
			settingsFile = args[0];
			logConfigFile = args[1];
		}
		
		// Set up logging
		PropertyConfigurator.configure(logConfigFile);
		
		try {
			Settings.unmarshallSettings(settingsFile);
		} catch (Exception e) {
			LOGGER.fatal("Failed to load settings from file: " + settingsFile);
			System.exit(1);
		}
		
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
		
		LOGGER.info("=======================================================================================");
		LOGGER.info("Starting the application.");
		
		
		// Start the applet
		args = new String[] {"--location=0,0", "--full-screen", "--hide-stop"};
		PApplet.main("ca.uwaterloo.epad.Application", args);
	}
}