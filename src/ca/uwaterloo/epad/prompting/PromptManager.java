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
import java.util.Collections;
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
	private static boolean wasBrushPromptDisplayed = false, wasPaintPromptDisplayed = false;
	
	private static PromptPopup brushPrompt, paintPrompt, engagementPromp;
	private static int promptStep = 1;
	
	private static PromptManager instance;
	private static boolean isInitialized = false;
	
	private static ResourceBundle promptStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.Prompts", Settings.locale);
	
	private static Drawer leftDrawer, rightDrawer, topDrawer;
	private static boolean wasLeftDrawerOpened = false, wasRightDrawerOpened = false;
	private static Zone focalItem;
	private static Timer tempTimer, brushPromptTimer, paintPromptTimer;
	
	private PromptManager() {};
	
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
		
		isInitialized = true;
	}
	
	public static void add(PromptPopup pp) {
		activePrompts.add(pp);
	}
	
	public static void draw() {
		if (!isInitialized) return;
		
		for (int i=0; i < activePrompts.size(); i++) {
			PromptPopup pp = activePrompts.get(i);
			if (pp.isInvisible()) {
				activePrompts.remove(i);
				if (pp == brushPrompt)
					brushPrompt = null;
				else if (pp == paintPrompt)
					paintPrompt = null;
			}
			else pp.draw();
		}
	}
	
	public static void pre() {
		if (!isInitialized) return;
		
		appInactiveTime = Application.getInactiveTime();
		
		//TODO: show entrance prompt asking user for a name to keep in the database
		
		if (appInactiveTime >= Settings.resetPromptDelay) {
			//TODO: show reset prompt after the application has been inactive for some time
		} else if (appInactiveTime >= Settings.engagementPromptDelay && activePrompts.size() == 0 && !leftDrawer.isOpen() && !rightDrawer.isOpen()) {
			if (tempTimer != null && !tempTimer.isTimeOut()) return;
				
			// show random engagement prompt to recapture attention
			if (Math.random() > 0.5 && Application.getAllBrushes().size() > 1) {
				// create a random brush engagement prompt
				ArrayList<MoveableItem> list = new ArrayList<MoveableItem>(Application.getAllBrushes());
				list.remove(Application.getSelectedBrush());
				Collections.shuffle(list);
				MoveableItem item = list.get(0);
				
				PVector v = item.getCentre();
				engagementPromp = new PromptPopup((int) v.x, (int) v.y, promptStrings.getString("engagementPromptIcon"), promptStrings.getString("engagementPromptTextBrush"));
				PromptManager.add(engagementPromp);
				TTSManager.say(promptStrings.getString("engagementPromptTextBrush"));
			} else if (Application.getAllPaints().size() > 1) {
				// create a random paint engagement prompt
				ArrayList<MoveableItem> list = new ArrayList<MoveableItem>(Application.getAllPaints());
				list.remove(Application.getSelectedPaint());
				Collections.shuffle(list);
				MoveableItem item = list.get(0);
				
				PVector v = item.getCentre();
				engagementPromp = new PromptPopup((int) v.x, (int) v.y, promptStrings.getString("engagementPromptIcon"), promptStrings.getString("engagementPromptTextPaint"));
				PromptManager.add(engagementPromp);
				TTSManager.say(promptStrings.getString("engagementPromptTextPaint"));
			}
			
			tempTimer = new Timer(Settings.engagementPromptDelay);
		} else if (!wasLeftDrawerOpened && !wasBrushPromptDisplayed && brushPromptTimer.isTimeOut() && activePrompts.size() == 0) {
			// Show setup prompt: brush
			PVector v = leftDrawer.getHandleLocation();
			brushPrompt = new PromptPopup((int) v.x, (int) v.y, promptStrings.getString("brushPromptStep1Icon"), promptStrings.getString("brushPromptStep1Text"));
			PromptManager.add(brushPrompt);
			TTSManager.say(promptStrings.getString("brushPromptStep1Text"));
			wasBrushPromptDisplayed = true;
			promptStep = 1;
		} else if (!wasRightDrawerOpened && !wasPaintPromptDisplayed && paintPromptTimer.isTimeOut() && activePrompts.size() == 0) {
			// Show setup prompt: paint
			PVector v = rightDrawer.getHandleLocation();
			paintPrompt = new PromptPopup((int) v.x, (int) v.y, promptStrings.getString("paintPromptStep1Icon"), promptStrings.getString("paintPromptStep1Text"));
			PromptManager.add(paintPrompt);
			TTSManager.say(promptStrings.getString("paintPromptStep1Text"));
			wasPaintPromptDisplayed = true;
			promptStep = 1;
		}
		
		// process prompt flow
		if (brushPrompt != null) {
			if (promptStep == 1) {
				PVector v = leftDrawer.getHandleLocation();
				brushPrompt.setCoordinates(v);
				
				if (leftDrawer.getVisibleWidth() > 200) {
					brushPrompt.setCoordinates(v.x - 270, v.y);
					brushPrompt.setIcon(promptStrings.getString("brushPromptStep2Icon"));
					brushPrompt.setText(promptStrings.getString("brushPromptStep2Text"));
					TTSManager.say(promptStrings.getString("brushPromptStep2Text"));
					leftDrawer.getContainer().addListener(instance);
					tempTimer = new Timer(5000);
					promptStep++;
				}
			} else if (promptStep == 2) {
				PVector v = leftDrawer.getHandleLocation();
				brushPrompt.setCoordinates(v.x - 270, v.y);
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
				
				brushPrompt.setCoordinates(v);
			} else if (promptStep == 4) {
				if (tempTimer.isTimeOut()) {
					if (leftDrawer.isOpen()) {
						PVector v = leftDrawer.getHandleLocation();
						brushPrompt.setCoordinates(v);
						brushPrompt.setIcon(promptStrings.getString("brushPromptStep5Icon"));
						brushPrompt.setText(promptStrings.getString("brushPromptStep5Text"));
						TTSManager.say(promptStrings.getString("brushPromptStep5Text"));
						promptStep++;
					} else {
						brushPrompt.dispose();
					}
					focalItem = null;
				}
				
				if (focalItem != null) {
					PVector v = focalItem.getCentre();
					brushPrompt.setCoordinates(v);
				}
			} else if (promptStep == 5) {
				PVector v = leftDrawer.getHandleLocation();
				brushPrompt.setCoordinates(v);
			}
		}
		
		if (paintPrompt != null) {
			if (promptStep == 1) {
				PVector v = rightDrawer.getHandleLocation();
				paintPrompt.setCoordinates(v);
				
				if (rightDrawer.getVisibleWidth() > 200) {
					paintPrompt.setCoordinates(v.x + 270, v.y);
					paintPrompt.setIcon(promptStrings.getString("paintPromptStep2Icon"));
					paintPrompt.setText(promptStrings.getString("paintPromptStep2Text"));
					TTSManager.say(promptStrings.getString("paintPromptStep2Text"));
					rightDrawer.getContainer().addListener(instance);
					tempTimer = new Timer(5000);
					promptStep++;
				}
			} else if (promptStep == 2) {
				PVector v = rightDrawer.getHandleLocation();
				paintPrompt.setCoordinates(v.x + 270, v.y);
				if (tempTimer != null && tempTimer.isTimeOut()) {
					paintPrompt.setIcon(promptStrings.getString("paintPromptStep3Icon"));
					paintPrompt.setText(promptStrings.getString("paintPromptStep3Text"));
					TTSManager.say(promptStrings.getString("paintPromptStep3Text"));
					tempTimer = null;
					rightDrawer.getContainer().removeListener(instance);
					promptStep++;
				}
			} else if (promptStep == 3) {
				if (focalItem != null && focalItem instanceof MoveableItem) {
					if (!((MoveableItem)focalItem).getIsDragged()) {
						paintPrompt.setIcon(promptStrings.getString("paintPromptStep4Icon"));
						paintPrompt.setText(promptStrings.getString("paintPromptStep4Text"));
						TTSManager.say(promptStrings.getString("paintPromptStep4Text"));
						tempTimer = new Timer(6000);
						promptStep++;
					}
				}
				
				PVector v = new PVector();
				if (focalItem != null) {
					v = focalItem.getCentre();
				} else {
					Zone item = rightDrawer.getContainer().getItemByID(1);
					v = item.getCentre();
				}
				
				paintPrompt.setCoordinates(v);
			} else if (promptStep == 4) {
				if (tempTimer.isTimeOut()) {
					if (rightDrawer.isOpen()) {
						PVector v = rightDrawer.getHandleLocation();
						paintPrompt.setCoordinates(v);
						paintPrompt.setIcon(promptStrings.getString("paintPromptStep5Icon"));
						paintPrompt.setText(promptStrings.getString("paintPromptStep5Text"));
						TTSManager.say(promptStrings.getString("paintPromptStep5Text"));
						promptStep++;
					} else {
						paintPrompt.dispose();
					}
					focalItem = null;
				}
				
				if (focalItem != null) {
					PVector v = focalItem.getCentre();
					paintPrompt.setCoordinates(v);
				}
			} else if (promptStep == 5) {
				PVector v = rightDrawer.getHandleLocation();
				paintPrompt.setCoordinates(v);
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
				if (paintPrompt != null && promptStep == 1)
					paintPrompt.hideText();
			}
		} else if (event.getActionCommand().equals(Drawer.CLOSED)) {
			if (brushPrompt != null && event.getSource() == leftDrawer && promptStep == 5) {
				brushPrompt.dispose();
			} else if (paintPrompt != null && event.getSource() == rightDrawer && promptStep == 5) {
				System.out.println("exit1");
				paintPrompt.dispose();
			}
		} else if (event.getActionCommand().equals(Application.ITEM_ADDED)) {
			if (brushPrompt != null && promptStep == 3 && focalItem == null) {
				focalItem = (Zone) event.getSource();
				brushPrompt.hideText();
			} else if (paintPrompt != null && promptStep == 3 && focalItem == null) {
				focalItem = (Zone) event.getSource();
				paintPrompt.hideText();
			}
		} else if (event.getActionCommand().equals(Application.ITEM_REMOVED)) {
			if (brushPrompt != null && promptStep == 3 && event.getSource() == focalItem) {
				focalItem = null;
				brushPrompt.dispose();
			} else if (paintPrompt != null && promptStep == 3 && event.getSource() == focalItem) {
				focalItem = null;
				paintPrompt.dispose();
			}
		}  else if (event.getActionCommand().equals(Application.PAINT_SELECTED) || event.getActionCommand().equals(Application.BRUSH_SELECTED)) {
			if (engagementPromp != null) {
				engagementPromp.dispose();
				TTSManager.stop();
			}
		}
	}
}
