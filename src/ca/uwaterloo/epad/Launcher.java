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

import java.util.ResourceBundle;

import ca.uwaterloo.epad.ui.SplashScreen;
import ca.uwaterloo.epad.util.Settings;

import processing.core.PApplet;

public class Launcher {
	private static ResourceBundle uiStrings;
	
	public static void main(String args[]) {
		try {
			Settings.unmarshallSettings();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		uiStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.UI", Settings.locale);
		
		// Create a splash screen
		try {
			SplashScreen.splash(Settings.dataFolder + "images\\epadLogo.png");
			SplashScreen.setMessage(uiStrings.getString("LoadingMessage"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SplashScreen.setMessage(uiStrings.getString("StartingMessage"));
		
		// Start the applet
		PApplet.main(new String[] { "--present", "ca.uwaterloo.epad.Application" });
	}
}