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

package ca.uwaterloo.epad.prompting;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ResourceBundle;

import processing.core.PApplet;
import processing.core.PVector;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.Drawer;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.util.TTSManager;
import ca.uwaterloo.epad.util.Timer;

public class PromptManager implements ActionListener {
	private static ArrayList<PromptPopup> activePrompts = new ArrayList<PromptPopup>();
	protected static PApplet parent;
	
	private static long appInactiveTime;
	private static boolean wasBrushPromptDisplayed = false;
	
	private static PromptPopup brushPrompt;
	private static int promptStep = 1;
	
	private static PromptManager instance;
	
	private static ResourceBundle promptStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.Prompts", Settings.locale);
	
	private static Drawer leftDrawer, rightDrawer, topDrawer;
	private static boolean wasLeftDrawerOpened = false, wasRightDrawerOpened = false, wasTopDrawerOpened = false;
	private static Zone focalItem;
	private static Timer tempTimer, brushPromptTimer, paintPromptTimer;
	
	private PromptManager() {
		
	};
	
	public static void init(PApplet parent) {
		PromptManager.parent = parent;
		
		parent.registerMethod("draw", new PromptManager());
		parent.registerMethod("pre", new PromptManager());
		
		instance = new PromptManager();
		
		Application.addListener(instance);
		
		try {
			leftDrawer = Application.getDrawer(Application.LEFT_DRAWER);
			leftDrawer.addListener(instance);
			rightDrawer = Application.getDrawer(Application.RIGHT_DRAWER);
			rightDrawer.addListener(instance);
			topDrawer = Application.getDrawer(Application.TOP_DRAWER);
			topDrawer.addListener(instance);
		} catch (NullPointerException npe) {
			System.err.println("Error: PromptManager must be initialized after drawers are created");
		}
		
		// set up timers
		brushPromptTimer = new Timer(Settings.brushPromptDelay);
		paintPromptTimer = new Timer(Settings.paintPromptDelay);
	}
	
	public static void add(PromptPopup pp) {
		activePrompts.add(pp);
	}
	
	public static void remove(PromptPopup pp) {
		pp.dispose();
	}
	
	public static void clear() {
		activePrompts.clear();
	}
	
	public static void draw() {
		for (int i=0; i < activePrompts.size(); i++) {
			PromptPopup pp = activePrompts.get(i);
			if (pp.isInvisible()) activePrompts.remove(i);
			else pp.draw();
		}
	}
	
	public static void pre() {
		appInactiveTime = Application.getInactiveTime();
		
		if (appInactiveTime >= Settings.resetPromptDelay) {
			//TODO: show reset prompt after the application has been inactive for some time
		} else if (appInactiveTime >= Settings.randomPromptDelay) {
			//TODO: show random prompt to recapture attention
		} else if (!wasLeftDrawerOpened && !wasBrushPromptDisplayed && brushPromptTimer.isTimeOut()) {
			// Show setup prompt: brush
			PVector v = leftDrawer.getHandleLocation();
			brushPrompt = new PromptPopup((int) v.x, (int) v.y, promptStrings.getString("brushPromptStep1Icon"), promptStrings.getString("brushPromptStep1Text"));
			PromptManager.add(brushPrompt);
			TTSManager.say(promptStrings.getString("brushPromptStep1Text"));
			wasBrushPromptDisplayed = true;
		}
		
		// process prompt flow
		if (brushPrompt != null) {
			if (promptStep == 1) {
				PVector v = leftDrawer.getHandleLocation();
				brushPrompt.setX(v.x);
				brushPrompt.setY(v.y);
				
				if (leftDrawer.getVisibleWidth() > 200) {
					brushPrompt.setX(v.x - 100);
					brushPrompt.setY(v.y);
					brushPrompt.setIcon(promptStrings.getString("brushPromptStep2Icon"));
					brushPrompt.setText(promptStrings.getString("brushPromptStep2Text"));
					TTSManager.say(promptStrings.getString("brushPromptStep2Text"));
					leftDrawer.getContainer().addListener(instance);
					tempTimer = new Timer(5000);
					promptStep++;
				}
			} else if (promptStep == 2) {
				PVector v = leftDrawer.getHandleLocation();
				brushPrompt.setX(v.x - 200);
				brushPrompt.setY(v.y);
				if (tempTimer != null && tempTimer.isTimeOut()) {
					brushPrompt.setIcon(promptStrings.getString("brushPromptStep3Icon"));
					brushPrompt.setText(promptStrings.getString("brushPromptStep3Text"));
					TTSManager.say(promptStrings.getString("brushPromptStep3Text"));
					tempTimer = null;
					leftDrawer.getContainer().removeListener(instance);
					promptStep++;
				}
			} else if (promptStep == 3) {
				if (focalItem != null && focalItem instanceof MoveableItem) {
					if (!((MoveableItem)focalItem).getIsDragged()) {
						brushPrompt.setIcon(promptStrings.getString("brushPromptStep4Icon"));
						brushPrompt.setText(promptStrings.getString("brushPromptStep4Text"));
						TTSManager.say(promptStrings.getString("brushPromptStep4Text"));
						tempTimer = new Timer(7000);
						promptStep++;
					}
				}
				
				PVector v = new PVector();
				if (focalItem != null) {
					v = focalItem.getCentre();
				} else {
					Zone item = leftDrawer.getContainer().getItemByID(1);
					v = item.getCentre();
				}
				
				brushPrompt.setX(v.x);
				brushPrompt.setY(v.y);
			} else if (promptStep == 4) {
				if (focalItem != null) {
					PVector v = focalItem.getCentre();
					brushPrompt.setX(v.x);
					brushPrompt.setY(v.y);
				} else {
					brushPrompt.dispose();
				}
				
				if (tempTimer.isTimeOut() && leftDrawer.isOpen()) {
					PVector v = leftDrawer.getHandleLocation();
					brushPrompt.setX(v.x);
					brushPrompt.setY(v.y);
					brushPrompt.setIcon(promptStrings.getString("brushPromptStep5Icon"));
					brushPrompt.setText(promptStrings.getString("brushPromptStep5Text"));
					TTSManager.say(promptStrings.getString("brushPromptStep5Text"));
					promptStep++;
				}
			} else if (promptStep == 5) {
				PVector v = leftDrawer.getHandleLocation();
				brushPrompt.setX(v.x);
				brushPrompt.setY(v.y);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals(Drawer.OPEN)) {
			if (event.getSource() == leftDrawer) {
				wasLeftDrawerOpened = true;
				if (brushPrompt != null && promptStep == 1)
					brushPrompt.hideText();
			} else if (event.getSource() == rightDrawer) {
				wasRightDrawerOpened = true;
			} else if (event.getSource() == topDrawer) {
				wasTopDrawerOpened = true;
			}
		} else if (event.getActionCommand().equals(Drawer.CLOSED)) {
			if (event.getSource() == leftDrawer && promptStep == 5) {
				brushPrompt.dispose();
			}
		} else if (event.getActionCommand().equals(Application.ITEM_ADDED)) {
			if (brushPrompt != null && promptStep == 3 && focalItem == null) {
				focalItem = (Zone) event.getSource();
				brushPrompt.hideText();
			}
		} else if (event.getActionCommand().equals(Application.ITEM_REMOVED)) {
			if (brushPrompt != null && promptStep == 3 && event.getSource() == focalItem) {
				focalItem = null;
				remove(brushPrompt);
			}
		} /* else if (event.getActionCommand().equals(Container.MOVED)) {
			if (brushPrompt != null && promptStep == 2 && event.getSource() == leftDrawer.getContainer()) {
				brushPrompt.setIcon(promptStrings.getString("brushPromptStep3Icon"));
				brushPrompt.setText(promptStrings.getString("brushPromptStep3Text"));
				TTSManager.say(promptStrings.getString("brushPromptStep3Text"));
				tempTimer = null;
				leftDrawer.getContainer().removeListener(instance);
				promptStep++;
			}
		}*/
	}
}
