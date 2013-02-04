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

import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import processing.core.PFont;
import vialab.SMT.KeyboardZone;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.xml.SaveFile;

/**
 * This dialog allows user to save the entire workspace and enter their name so
 * that it can be retrieved later.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class SaveDialog extends Zone {
	private static final Logger LOGGER = Logger.getLogger(SaveDialog.class);

	// Colours
	public int backgroundColour = Application.backgroundColour;
	public int borderColour = Application.primaryColour;
	public int transparentColour = Application.transparentColour;
	public int transparentAlpha = Application.transparentAlpha;
	public int textColour = Application.textColour;
	public int headerColour = Application.secondaryColour;

	// Bundle of UI strings
	private static ResourceBundle uiStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.UI", Settings.locale);

	// Layout parameters
	private int outerPadding = 70;
	private int innerPadding = 30;
	private int headerSize = 50;
	private int helpTextSize = 20;
	private int keyboardHeight = 250;
	private int dialogWidth, dialogHeight;
	private int buttonWidth = headerSize * 8;
	private int buttonHeight = headerSize + 20;
	private int buttonSpacing = 40;

	// Other variables
	private String headerText, helpText, mainText = "";
	private static PFont headerFont, smallFont;
	private KeyboardZone keyboard;
	private Button saveButton, cancelButton;

	// Flag indicating that the dialog is currently on screen
	private static boolean isOnScreen;

	/**
	 * Default constructor.
	 */
	public SaveDialog() {
		super(0, 0, applet.width, applet.height);

		LOGGER.info("SaveDialog opened.");

		// Pause the application while the dialog is on the screen
		Application.pauseApplication();
		isOnScreen = true;

		// Calculate the dimensions based on widget size and padding
		dialogWidth = applet.width - outerPadding * 2;
		dialogHeight = applet.height - outerPadding * 2;

		// Create fonts and retrieve strings from the bundle
		headerFont = applet.createFont("Arial", headerSize);
		smallFont = applet.createFont("Arial", helpTextSize);
		headerText = uiStrings.getString("SaveDialogHeaderText");
		helpText = uiStrings.getString("SaveDialogHelpText");

		// Create keyboard and buttons
		int kX = innerPadding + outerPadding;
		int kY = outerPadding + dialogHeight - innerPadding - keyboardHeight - buttonSpacing - buttonHeight;

		keyboard = new KeyboardZone(kX, kY, dialogWidth - innerPadding * 2, keyboardHeight, false);
		add(keyboard);
		keyboard.addKeyListener(this);
		keyboard.backgroundColor = Application.backgroundColour;
		keyboard.keyColor = Application.primaryColour;
		keyboard.keyPressedColor = Application.secondaryColour;
		keyboard.linkColor = 0x00FFFFFF;

		int bX = outerPadding + (dialogWidth - innerPadding * 2 - buttonWidth - buttonSpacing) / 2;
		int bY = outerPadding + dialogHeight - innerPadding - buttonHeight - buttonSpacing / 2;

		saveButton = new Button(bX, bY, buttonWidth / 2, buttonHeight, uiStrings.getString("SaveButton"), headerSize, headerFont);
		saveButton.setPressMethod("doSave", this);
		add(saveButton);

		bX += buttonWidth / 2 + buttonSpacing;

		cancelButton = new Button(bX, bY, buttonWidth / 2, buttonHeight, uiStrings.getString("CancelButton"), headerSize, headerFont);
		cancelButton.setPressMethod("close", this);
		add(cancelButton);

		// Add a close button
		add(new CloseButton(dialogWidth + outerPadding - 50, outerPadding - 25, 75, 75));
	}

	// Draw the dialog
	@Override
	protected void drawImpl() {
		// Darken the screen
		noStroke();
		fill(transparentColour, transparentAlpha);
		rectMode(CORNER);
		rect(0, 0, width, height);

		// Border
		stroke(borderColour);
		strokeWeight(2);
		fill(transparentColour, transparentAlpha);
		rect(outerPadding, outerPadding, dialogWidth, dialogHeight, 30);

		// Dialog background
		noStroke();
		fill(backgroundColour);
		rect(outerPadding + innerPadding, outerPadding + innerPadding, dialogWidth - innerPadding * 2, dialogHeight - innerPadding * 2);

		// Header background
		fill(headerColour);
		rect(outerPadding + innerPadding, outerPadding + innerPadding, dialogWidth - innerPadding * 2, headerSize + 5);

		// Header text
		fill(textColour);
		textFont(headerFont);
		textAlign(CENTER, CENTER);
		text(headerText, outerPadding + innerPadding, outerPadding + innerPadding, dialogWidth - innerPadding * 2, headerSize);

		// Help text
		textFont(smallFont);
		text(helpText, outerPadding + innerPadding, outerPadding + innerPadding + headerSize + 5, dialogWidth - innerPadding * 2, headerSize);

		// Main text background
		fill(transparentColour, transparentAlpha);
		rect(outerPadding + innerPadding * 2, outerPadding + innerPadding + headerSize * 2 + 5, dialogWidth - innerPadding * 4, keyboard.y - (outerPadding + innerPadding + headerSize + 5));

		// Main text
		fill(textColour);
		textFont(headerFont);
		text(mainText, outerPadding + innerPadding * 2, outerPadding + innerPadding + headerSize * 2 + 5, dialogWidth - innerPadding * 4, keyboard.y - (outerPadding + innerPadding + headerSize + 5));
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
	 * Close the dialog. This method may be called by the close button.
	 * 
	 * @see CloseButton
	 */
	public void close() {
		LOGGER.info("SaveDialog closed.");
		TouchClient.remove(this);
		Application.resumeApplication();
		isOnScreen = false;
	}

	/**
	 * Save the workspace using the entered name.
	 * 
	 * @see SaveFile#save(String)
	 */
	public void doSave() {
		LOGGER.info("SaveDialog saving the workspace.");
		
		SaveFile sf = new SaveFile();
		boolean success = sf.save(mainText);

		// Change text to indicate success
		if (success)
			mainText = uiStrings.getString("SaveDialogSucessText");
		
		//TODO: display a message is file was not saved properly

		// Remove keyboard and buttons Save and Cancel
		remove(keyboard);
		remove(saveButton);
		remove(cancelButton);

		// Show Continue and Reset buttons
		int bX = outerPadding + (dialogWidth - innerPadding * 2 - buttonWidth * 2 - buttonSpacing) / 2;
		int bY = outerPadding - innerPadding * 2 + dialogHeight - keyboardHeight;

		Button b = new Button(bX, bY, buttonWidth, buttonHeight, uiStrings.getString("ContinueButton"), headerSize, headerFont);
		b.setPressMethod("close", this);
		add(b);

		bX += buttonWidth + buttonSpacing;

		b = new Button(bX, bY, buttonWidth, buttonHeight, uiStrings.getString("ResetButton"), headerSize, headerFont);
		b.setStaticPressMethod("resetToDefaults", Application.class);
		add(b);
	}

	// Listen to key events and add characters to the main text as they are
	// entered
	@Override
	protected void keyTypedImpl(KeyEvent e) {
		if (e.getKeyChar() == '\b' && mainText.length() > 0) {
			// If a backspace is entered, remove the last character
			mainText = mainText.substring(0, mainText.length() - 1);
		} else if (e.getKeyChar() == '\n') {
			// Save on Enter
			doSave();
		} else {
			mainText += e.getKeyChar();
		}
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
