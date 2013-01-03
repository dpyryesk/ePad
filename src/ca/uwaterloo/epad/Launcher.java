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