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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import processing.core.PFont;

import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.util.TTSManager;
import ca.uwaterloo.epad.util.Timer;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;

/**
 * This dialog is displayed after a certain period of inactivity (as specified
 * in settings.xml) and offers the user several options: save, print, reset or
 * continue painting. If no action is selected, the dialog resets the
 * application after a certain amount of time (the countdown to reset is
 * displayed in the dialog as well).
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see Application#resetToDefaults()
 * 
 */
public class ResetDialog extends Zone {
	private static final Logger LOGGER = Logger.getLogger(ResetDialog.class);

	// Colours
	public int backgroundColour = Application.backgroundColour;
	public int borderColour = Application.primaryColour;
	public int transparentColour = Application.transparentColour;
	public int transparentAlpha = Application.transparentAlpha;
	public int textColour = Application.textColour;

	// Bundle of UI strings
	private static ResourceBundle uiStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.UI", Settings.locale);
	// Message formatter object
	private static MessageFormat formatter = new MessageFormat(uiStrings.getString("ResetDialogResetText"), Settings.locale);

	// Layout parameters
	private int dialogWidth = 800;
	private int dialogHeight = 600;
	private int dialogX, dialogY;
	private int padding = 30;
	private int textSize = 40;
	private int buttonWidth;
	private int buttonHeight = textSize + 20;
	private int buttonSpacing = 40;

	// Other variables
	private PFont font;
	private String mainText;
	private String resetText;
	private Timer resetTimer;

	// Flag indicating that the dialog is currently on screen
	private static boolean isOnScreen;

	/**
	 * Default constructor.
	 */
	public ResetDialog() {
		super(0, 0, applet.width, applet.height);

		LOGGER.info("ResetDialog opened.");

		// Pause the application while the dialog is on the screen
		Application.pauseApplication();
		isOnScreen = true;
		resetTimer = new Timer(Settings.resetDelay);

		// Calculate the position of the dialog
		dialogX = (width - dialogWidth) / 2;
		dialogY = (height - dialogHeight) / 2;
		
		// Create fonts and retrieve strings from the bundle
		font = applet.createFont("Arial", textSize);
		mainText = uiStrings.getString("ResetDialogMainText");

		// Create buttons
		buttonWidth = (dialogWidth - buttonSpacing * 4) / 3;
		int buttonX = dialogX + buttonSpacing;
		int buttonY = dialogY + dialogHeight / 4 + buttonSpacing;

		Button b = new Button(buttonX, buttonY, buttonWidth, buttonHeight, uiStrings.getString("SaveButton"), textSize, font);
		b.setPressMethod("doSave", this);
		add(b);

		buttonX += buttonWidth + buttonSpacing;

		b = new Button(buttonX, buttonY, buttonWidth, buttonHeight, uiStrings.getString("PrintButton"), textSize, font);
		b.setPressMethod("doPrint", this);
		add(b);

		buttonX += buttonWidth + buttonSpacing;

		b = new Button(buttonX, buttonY, buttonWidth, buttonHeight, uiStrings.getString("ContinueButton"), textSize, font);
		b.setPressMethod("close", this);
		add(b);

		buttonX = dialogX + buttonSpacing * 2 + buttonWidth;
		buttonY += buttonSpacing * 2 + buttonHeight;

		b = new Button(buttonX, buttonY, buttonWidth, buttonHeight, uiStrings.getString("ResetButton"), textSize, font);
		b.setPressMethod("doReset", this);
		add(b);

		// Add a close button
		add(new CloseButton(dialogX + dialogWidth - 25, dialogY - 50, 75, 75));

		// Say the header text
		TTSManager.say(mainText);
	}

	// Draw the dialog
	@Override
	protected void drawImpl() {
		// Check the reset timer and reset the application when it runs out
		if (resetTimer.isTimeOut())
			doReset();

		// Darken the screen
		noStroke();
		fill(transparentColour, transparentAlpha);
		rectMode(CORNER);
		rect(0, 0, width, height);

		// Border
		stroke(borderColour);
		strokeWeight(2);
		fill(transparentColour, transparentAlpha);
		rect(dialogX - padding, dialogY - padding, dialogWidth + padding * 2, dialogHeight + padding * 2, padding);

		// Dialog background
		noStroke();
		fill(backgroundColour);
		rect(dialogX, dialogY, dialogWidth, dialogHeight);

		int textY = dialogY;
		int textH = dialogHeight / 4;

		// Header background
		fill(borderColour);
		rect(dialogX, textY, dialogWidth, textH);

		// Header text
		fill(textColour);
		textFont(font);
		textAlign(CENTER, CENTER);
		text(mainText, dialogX, textY, dialogWidth, textH);

		// Show the remaining time until reset
		int secondsLeft = (Settings.resetDelay - (int) resetTimer.getTimePassed()) / 1000;
		int minsLeft = (int) Math.floor((double) secondsLeft / 60);
		secondsLeft -= minsLeft * 60;

		Object[] args = { new Integer(minsLeft), new Integer(secondsLeft) };
		resetText = formatter.format(args);
		textY = dialogY + dialogHeight - textH;
		text(resetText, dialogX, textY, dialogWidth, textH);
	}

	// Draw for zone picker
	@Override
	protected void pickDrawImpl() {
		rect(0, 0, width, height);
	}

	// Action on the touch event
	@Override
	protected void touchImpl() {
		Application.setActionPerformed();
	}

	/**
	 * Close the dialog and save the workspace.
	 * 
	 * @see Application#save()
	 */
	public void doSave() {
		close();
		Application.save();
	}

	/**
	 * Close the dialog and print the painting.
	 * 
	 * @see Application#print()
	 */
	public void doPrint() {
		close();
		Application.print();
	}

	/**
	 * Close the dialog and reset the application.
	 * 
	 * @see Application#resetToDefaults()
	 */
	public void doReset() {
		close();
		Application.resetToDefaults();
	}

	/**
	 * Close the dialog. This method may be called by the close button.
	 * 
	 * @see CloseButton
	 */
	public void close() {
		LOGGER.info("ResetDialog closed.");
		TouchClient.remove(this);
		Application.resumeApplication();
		isOnScreen = false;
	}

	/**
	 * 
	 * @return <b>true</b> if the dialog is currently displayed on screen and
	 *         <b>false</b> otherwise.
	 */
	public static boolean isOnScreen() {
		return isOnScreen;
	}
}
