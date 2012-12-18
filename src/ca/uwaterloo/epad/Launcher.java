package ca.uwaterloo.epad;

import processing.core.PApplet;

public class Launcher {
	public static void main(String args[]) {
		// Create a splash screen
		try {
			SplashScreen.splash("data\\images\\epadLogo.png");
			SplashScreen.setMessage("Loading settings...");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SplashScreen.setMessage("Starting...");
		
		// Start the applet
		PApplet.main(new String[] { "--present", "ca.uwaterloo.epad.Application" });
	}
}