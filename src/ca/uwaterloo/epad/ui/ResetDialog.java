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

public class ResetDialog extends Zone {
	private static final Logger LOGGER = Logger.getLogger(ResetDialog.class);
	
	public int backgroundColour = Application.backgroundColour;
	public int borderColour = Application.primaryColour;
	public int transparentColour = Application.transparentColour;
	public int transparentAlpha = Application.transparentAlpha;
	public int textColour = Application.textColour;
	
	private static ResourceBundle uiStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.UI", Settings.locale);
	private static MessageFormat formatter = new MessageFormat(uiStrings.getString("ResetDialogResetText"), Settings.locale);
	
	private int dialogWidth = 800;
	private int dialogHeight = 600;
	private int dialogX, dialogY;
	private int padding = 30;
	private int textSize = 40;
	private int buttonWidth;
	private int buttonHeight = textSize + 20;
	private int bGap = 40;
	
	private PFont font = applet.createFont("Arial", textSize);
	private String mainText = uiStrings.getString("ResetDialogMainText");
	private String resetText;
	private Timer resetTimer;
	
	private static boolean isOnScreen;
	
	public ResetDialog () {
		super(0, 0, applet.width, applet.height);
		
		LOGGER.info("ResetDialog opened.");
		
		Application.pauseApplication();
		isOnScreen = true;
		resetTimer = new Timer(Settings.resetDelay);
		
		dialogX = (width - dialogWidth)/2;
		dialogY = (height - dialogHeight)/2;
		
		add(new CloseButton(dialogX + dialogWidth - 25, dialogY - 50, 75, 75));
		TTSManager.say(mainText);
		
		buttonWidth = (dialogWidth - bGap * 4)/3;
		int buttonX = dialogX + bGap;
		int buttonY = dialogY + dialogHeight / 4 + bGap;
		
		Button b = new Button(buttonX, buttonY, buttonWidth, buttonHeight, uiStrings.getString("SaveButton"), textSize, font);
		b.setPressMethod("doSave", this);
		add(b);
		
		buttonX += buttonWidth + bGap;
		
		b = new Button(buttonX, buttonY, buttonWidth, buttonHeight, uiStrings.getString("PrintButton"), textSize, font);
		b.setPressMethod("doPrint", this);
		add(b);
		
		buttonX += buttonWidth + bGap;
		
		b = new Button(buttonX, buttonY, buttonWidth, buttonHeight, uiStrings.getString("ContinueButton"), textSize, font);
		b.setPressMethod("close", this);
		add(b);
		
		buttonX = dialogX + bGap * 2 + buttonWidth;
		buttonY += bGap * 2 + buttonHeight;
		
		b = new Button(buttonX, buttonY, buttonWidth, buttonHeight, uiStrings.getString("ResetButton"), textSize, font);
		b.setPressMethod("doReset", this);
		add(b);
	}
	
	protected void drawImpl() {
		// check timer
		if (resetTimer.isTimeOut())
			doReset();
		
		noStroke();
		fill(transparentColour, transparentAlpha);
		rectMode(CORNER);
		rect(0, 0, width, height);
		
		stroke(borderColour);
		strokeWeight(2);
		fill(transparentColour, transparentAlpha);
		rect(dialogX - padding, dialogY - padding, dialogWidth + padding*2, dialogHeight + padding*2, padding);
		
		noStroke();
		fill(backgroundColour);
		rect(dialogX, dialogY, dialogWidth, dialogHeight);
		
		int textY = dialogY;
		int textH = dialogHeight / 4;
		
		fill(borderColour);
		rect(dialogX, textY, dialogWidth, textH);
		
		fill(textColour);
		textFont(font);
		textAlign(CENTER, CENTER);
		text(mainText, dialogX, textY, dialogWidth, textH);
		
		// show remaining time until reset
		int secondsLeft = (Settings.resetDelay - (int)resetTimer.getTimePassed())/1000;
		int minsLeft = (int) Math.floor((double)secondsLeft / 60);
		secondsLeft -= minsLeft * 60;
		
		Object[] args = {new Integer(minsLeft), new Integer(secondsLeft)};
		resetText = formatter.format(args);
		textY = dialogY + dialogHeight - textH;
		text(resetText, dialogX, textY, dialogWidth, textH);
	}
	
	protected void pickDrawImpl() {
		rect(0, 0, width, height);
	}
	
	protected void touchImpl() {
		Application.setActionPerformed();
	}
	
	public void doSave() {
		close();
		Application.save();
	}
	
	public void doPrint() {
		close();
		Application.print();
	}
	
	public void doReset() {
		close();
		Application.resetToDefaults();
	}
	
	public void close() {
		LOGGER.info("ResetDialog closed.");
		TouchClient.remove(this);
		Application.resumeApplication();
		isOnScreen = false;
	}
	
	public static boolean IsOnScreen() {
		return isOnScreen;
	}
}
