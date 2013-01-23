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

import processing.core.PFont;
import vialab.SMT.KeyboardZone;
import vialab.SMT.TouchClient;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.xml.SaveFile;

public class SaveDialog extends Zone {
	public int backgroundColour = Application.backgroundColour;
	public int borderColour = Application.primaryColour;
	public int transparentColour = Application.transparentColour;
	public int transparentAlpha = Application.transparentAlpha;
	public int textColour = Application.textColour;
	public int headerColour = Application.secondaryColour;
	
	private int outerPadding = 70;
	private int innerPadding = 30;
	private int headerSize = 50;
	private int helpTextSize = 20;
	private int keyboardHeight = 250;
	private int dialogWidth, dialogHeight;
	private int buttonWidth = headerSize * 8;
	private int buttonHeight = headerSize + 20;
	private int bGap = 40;
	
	private String headerText, helpText, mainText = "";
	private static PFont headerFont, smallFont;
	private static ResourceBundle uiStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.UI", Settings.locale);
	private KeyboardZone keyboard;
	private Button saveButton, cancelButton;
	
	private static boolean isOnScreen;
	
	public SaveDialog() {
		super(0, 0, applet.width, applet.height);
		
		Application.pauseApplication();
		isOnScreen = true;

		dialogWidth = applet.width - outerPadding * 2;
		dialogHeight = applet.height - outerPadding * 2;
		
		headerFont = applet.createFont("Arial", headerSize);
		smallFont = applet.createFont("Arial", helpTextSize);
		headerText = uiStrings.getString("SaveDialogHeaderText");
		helpText = uiStrings.getString("SaveDialogHelpText");
		
		add(new CloseButton(dialogWidth + outerPadding - 50, outerPadding - 25, 75, 75));
		
		keyboard = new KeyboardZone(innerPadding + outerPadding, outerPadding - innerPadding + dialogHeight - keyboardHeight, dialogWidth - innerPadding*2, keyboardHeight, false);
		add(keyboard);
		keyboard.addKeyListener(this);
		keyboard.backgroundColor = Application.backgroundColour;
		keyboard.keyColor = Application.primaryColour;
		keyboard.keyPressedColor = Application.secondaryColour;
		
		int bX = outerPadding + (dialogWidth - innerPadding * 2 - buttonWidth - bGap) / 2;
		int bY = outerPadding - innerPadding + dialogHeight - keyboardHeight - buttonHeight;
		
		saveButton = new Button(bX, bY, buttonWidth/2, buttonHeight, uiStrings.getString("SaveButton"), headerSize, headerFont);
		saveButton.setPressMethod("doSave", this);
		add(saveButton);
		
		bX += buttonWidth/2 + bGap;
		
		cancelButton = new Button(bX, bY, buttonWidth/2, buttonHeight, uiStrings.getString("CancelButton"), headerSize, headerFont);
		cancelButton.setPressMethod("close", this);
		add(cancelButton);
	}
	
	protected void drawImpl() {
		noStroke();
		fill(transparentColour, transparentAlpha);
		rectMode(CORNER);
		rect(0, 0, width, height);

		stroke(borderColour);
		strokeWeight(2);
		fill(transparentColour, transparentAlpha);
		rect(outerPadding, outerPadding, dialogWidth, dialogHeight, 30);

		noStroke();
		fill(backgroundColour);
		rect(outerPadding + innerPadding, outerPadding + innerPadding, dialogWidth - innerPadding * 2, dialogHeight - innerPadding * 2);
		
		fill(headerColour);
		rect(outerPadding + innerPadding, outerPadding + innerPadding, dialogWidth - innerPadding * 2, headerSize + 5);
		
		fill(textColour);
		textFont(headerFont);
		textAlign(CENTER, CENTER);
		text(headerText, outerPadding + innerPadding, outerPadding + innerPadding, dialogWidth - innerPadding * 2, headerSize);
		
		textFont(smallFont);
		text(helpText, outerPadding + innerPadding, outerPadding + innerPadding + headerSize + 5, dialogWidth - innerPadding * 2, headerSize);
		
		textFont(headerFont);
		text(mainText, outerPadding + innerPadding * 2, outerPadding + innerPadding + headerSize * 2 + 5, dialogWidth - innerPadding * 4, dialogHeight - innerPadding * 2 - headerSize - 5 - keyboardHeight - buttonHeight);
	}
	
	protected void pickDrawImpl() {
		rect(0, 0, width, height);
	}
	
	protected void touchImpl() {
		Application.setActionPerformed();
	}
	
	public void close() {
		TouchClient.remove(this);
		Application.resumeApplication();
		isOnScreen = false;
	}
	
	public void doSave() {
		String name = mainText;
		mainText = uiStrings.getString("SaveDialogSavingText");
		
		SaveFile sf = new SaveFile();
		sf.save(name);
		
		mainText = uiStrings.getString("SaveDialogSucessText");
		
		remove(keyboard);
		remove(saveButton);
		remove(cancelButton);
		
		// show buttons
		int bX = outerPadding + (dialogWidth - innerPadding * 2 - buttonWidth * 2 - bGap) / 2;
		int bY = outerPadding - innerPadding * 2 + dialogHeight - keyboardHeight;
		
		Button b = new Button(bX, bY, buttonWidth, buttonHeight, uiStrings.getString("ContinueButton"), headerSize, headerFont);
		b.setPressMethod("close", this);
		add(b);
		
		bX += buttonWidth + bGap;
		
		b = new Button(bX, bY, buttonWidth, buttonHeight, uiStrings.getString("ResetButton"), headerSize, headerFont);
		b.setStaticPressMethod("resetToDefaults", Application.class);
		add(b);
	}
	
	protected void keyTypedImpl(KeyEvent e) {
		if (e.getKeyChar() == '\b' && mainText.length() > 0) {
			mainText = mainText.substring(0, mainText.length()-1);
		} else if (e.getKeyChar() == '\n') {
			doSave();
		} else {
			mainText += e.getKeyChar();
		}
	}
	
	public static boolean IsOnScreen() {
		return isOnScreen;
	}
}
