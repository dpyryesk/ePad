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

import org.apache.log4j.Logger;

import processing.core.PApplet;
import processing.core.PVector;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.Drawer;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.util.Settings;
import ca.uwaterloo.epad.util.TTSManager;
import ca.uwaterloo.epad.util.Timer;

/**
 * This class manages the prompts, determines when they should be displayed and
 * controls their flow.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see PromptPopup
 */
public class PromptManager implements ActionListener {
	private static final Logger LOGGER = Logger.getLogger(PromptManager.class);

	// Array of currently visible prompts
	private static ArrayList<PromptPopup> activePrompts = new ArrayList<PromptPopup>();
	// Parent applet
	protected static PApplet parent;

	// How long the application has been inactive (in milliseconds)
	private static long appInactiveTime;
	// Prompt display flags
	private static boolean wasBrushPromptDisplayed, wasPaintPromptDisplayed;
	// Drawer state flags
	private static boolean wasLeftDrawerOpened, wasRightDrawerOpened;

	// Prompt variables
	private static PromptPopup brushPrompt, paintPrompt, engagementPromp;
	// Prompt timers
	private static Timer tempTimer, brushPromptTimer, paintPromptTimer, engagementTimer;
	// Counter for prompt progress
	private static int promptStep;

	// Resource bundle that contains the strings
	private static ResourceBundle promptStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.Prompts", Settings.locale);

	// The only instance of PromptManager class
	private static PromptManager instance;
	// PromptManager status flags
	private static boolean isInitialized = false;
	private static boolean isPaused;

	// Drawer pointers
	private static Drawer leftDrawer, rightDrawer, topDrawer;
	// Pointer to an item of interest
	private static Zone focalItem;

	// Private constructor to prevent instantiation
	private PromptManager() {
	};

	/**
	 * Initialise the Prompt Manager. This function should be called
	 * <i>after</i> the GUI is loaded, but <i>before</i> any prompt is diplayed.
	 * 
	 * @param parent parent applet
	 */
	public static void init(PApplet parent) {
		LOGGER.info("PromptManager initializing.");

		// Save a pointer to the parent applet
		PromptManager.parent = parent;

		// Register draw and pre methods to be called automatically
		parent.registerMethod("draw", new PromptManager());
		parent.registerMethod("pre", new PromptManager());

		// Create a single instance of the class
		instance = new PromptManager();

		// Add the instance as a listener to Application
		Application.addListener(instance);
	}

	/**
	 * Reset the state of the Prompt Manager and the timers. This function must
	 * be called <i>after</i> function {@link #init(PApplet)}.
	 */
	public static void reset() {
		LOGGER.info("PromptManager resetting.");

		// Check if the parent was set
		if (parent == null) {
			System.err.println("Error: PromptManager must be initialized first.");
			return;
		}

		// Clear the list of prompts and stop the Text-To-Speech engine
		activePrompts.clear();
		TTSManager.stop();

		// Get pointers to the drawers and add listeners
		try {
			leftDrawer = Application.getDrawer(Application.LEFT_DRAWER);
			leftDrawer.addListener(instance);
			rightDrawer = Application.getDrawer(Application.RIGHT_DRAWER);
			rightDrawer.addListener(instance);
			topDrawer = Application.getDrawer(Application.TOP_DRAWER);
			topDrawer.addListener(instance);
		} catch (NullPointerException e) {
			System.err.println("Error: PromptManager must be initialized after drawers are created.");
		}

		// Set up timers
		brushPromptTimer = new Timer(Settings.brushPromptDelay);
		paintPromptTimer = new Timer(Settings.paintPromptDelay);

		// Reset flags
		wasBrushPromptDisplayed = false;
		wasPaintPromptDisplayed = false;
		wasLeftDrawerOpened = false;
		wasRightDrawerOpened = false;
		promptStep = 1;

		// Set the state to initialised and running
		isInitialized = true;
		isPaused = false;
	}

	/**
	 * Pause all timers and all active prompts.
	 * 
	 * @see PromptPopup#pause()
	 * @see Timer#pause()
	 */
	public static void pause() {
		isPaused = true;
		if (tempTimer != null)
			tempTimer.pause();
		brushPromptTimer.pause();
		paintPromptTimer.pause();
		if (engagementTimer != null)
			engagementTimer.pause();

		for (int i = 0; i < activePrompts.size(); i++) {
			PromptPopup pp = activePrompts.get(i);
			pp.pause();
		}
	}

	/**
	 * Resume all timers and all active prompts.
	 * 
	 * @see PromptPopup#resume()
	 * @see Timer#resume()
	 */
	public static void resume() {
		isPaused = false;
		if (tempTimer != null)
			tempTimer.resume();
		brushPromptTimer.resume();
		paintPromptTimer.resume();
		if (engagementTimer != null)
			engagementTimer.pause();

		for (int i = 0; i < activePrompts.size(); i++) {
			PromptPopup pp = activePrompts.get(i);
			pp.resume();
		}
	}

	/**
	 * Add a prompt to the list of active prompts.
	 * 
	 * @param pp
	 *            a new prompt.
	 */
	public static void addPrompt(PromptPopup pp) {
		activePrompts.add(pp);
	}

	/**
	 * The draw loop calls the draw() method of every active prompt and also
	 * removes the prompts that exceeded their time to live and became
	 * invisible.
	 * 
	 * @see PromptPopup#draw()
	 */
	public static void draw() {
		if (!isInitialized || isPaused)
			return;

		for (int i = 0; i < activePrompts.size(); i++) {
			PromptPopup pp = activePrompts.get(i);
			// Remove invisible prompts
			if (pp.isInvisible()) {
				activePrompts.remove(i);
				if (pp == brushPrompt)
					brushPrompt = null;
				else if (pp == paintPrompt)
					paintPrompt = null;
			} else
				pp.draw();
		}
	}

	/**
	 * This functions runs before a draw() function is called and manages the
	 * creation and the flow of prompts.
	 */
	public static void pre() {
		if (!isInitialized || isPaused)
			return;

		// Get the time the application has been inactive
		appInactiveTime = Application.getInactiveTime();

		if (appInactiveTime >= Settings.engagementPromptDelay && activePrompts.size() == 0 && !leftDrawer.isOpen() && !rightDrawer.isOpen()) {
			if (engagementTimer == null) {
				// Start the repeat timer
				engagementTimer = new Timer(Settings.engagementPromptRepeatDelay);
			} else	if (!engagementTimer.isTimeOut()) {
				// Return if the repeat timer has not ran out yet
				return;
			}

			// Show a random engagement prompt to recapture attention
			if (Math.random() > 0.5 && Application.getAllBrushes().size() > 1) {
				// Create a random brush engagement prompt
				ArrayList<MoveableItem> list = new ArrayList<MoveableItem>(Application.getAllBrushes());
				list.remove(Application.getSelectedBrush());
				Collections.shuffle(list);
				MoveableItem item = list.get(0);

				PVector v = item.getCentre();
				engagementPromp = new PromptPopup((int) v.x, (int) v.y, promptStrings.getString("engagementPromptIcon"), promptStrings.getString("engagementPromptTextBrush"));
				PromptManager.addPrompt(engagementPromp);
				TTSManager.say(promptStrings.getString("engagementPromptTextBrush"), true);
			} else if (Application.getAllPaints().size() > 1) {
				// Create a random paint engagement prompt
				ArrayList<MoveableItem> list = new ArrayList<MoveableItem>(Application.getAllPaints());
				list.remove(Application.getSelectedPaint());
				Collections.shuffle(list);
				MoveableItem item = list.get(0);

				PVector v = item.getCentre();
				engagementPromp = new PromptPopup((int) v.x, (int) v.y, promptStrings.getString("engagementPromptIcon"), promptStrings.getString("engagementPromptTextPaint"));
				PromptManager.addPrompt(engagementPromp);
				TTSManager.say(promptStrings.getString("engagementPromptTextPaint"), true);
			}

			LOGGER.info("An engagement prompt displayed.");
			engagementTimer.restart();
		} else if (!wasLeftDrawerOpened && !wasBrushPromptDisplayed && brushPromptTimer.isTimeOut() && activePrompts.size() == 0) {
			// Show a setup prompt for brush drawer
			PVector v = leftDrawer.getHandleLocation();
			brushPrompt = new PromptPopup((int) v.x, (int) v.y, promptStrings.getString("brushPromptStep1Icon"), promptStrings.getString("brushPromptStep1Text"));
			PromptManager.addPrompt(brushPrompt);
			TTSManager.say(promptStrings.getString("brushPromptStep1Text"), true);
			wasBrushPromptDisplayed = true;
			promptStep = 1;
			LOGGER.info("The brush drawer prompt displayed.");
		} else if (!wasRightDrawerOpened && !wasPaintPromptDisplayed && paintPromptTimer.isTimeOut() && activePrompts.size() == 0) {
			// Show a setup prompt for paint drawer
			PVector v = rightDrawer.getHandleLocation();
			paintPrompt = new PromptPopup((int) v.x, (int) v.y, promptStrings.getString("paintPromptStep1Icon"), promptStrings.getString("paintPromptStep1Text"));
			PromptManager.addPrompt(paintPrompt);
			TTSManager.say(promptStrings.getString("paintPromptStep1Text"), true);
			wasPaintPromptDisplayed = true;
			promptStep = 1;
			LOGGER.info("The paint drawer prompt displayed.");
		}

		// Manage the prompt flow
		if (brushPrompt != null) {
			if (promptStep == 1) {
				PVector v = leftDrawer.getHandleLocation();
				brushPrompt.setCoordinates(v);

				if (leftDrawer.getVisibleWidth() > 200) {
					brushPrompt.setCoordinates(v.x - 270, v.y);
					brushPrompt.setIcon(promptStrings.getString("brushPromptStep2Icon"));
					brushPrompt.setText(promptStrings.getString("brushPromptStep2Text"));
					TTSManager.say(promptStrings.getString("brushPromptStep2Text"), false);
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
					TTSManager.say(promptStrings.getString("brushPromptStep3Text"), false);
					tempTimer = null;
					leftDrawer.getContainer().removeListener(instance);
					promptStep++;
				}
			} else if (promptStep == 3) {
				if (focalItem != null && focalItem instanceof MoveableItem) {
					if (!((MoveableItem) focalItem).getIsDragged()) {
						brushPrompt.setIcon(promptStrings.getString("brushPromptStep4Icon"));
						brushPrompt.setText(promptStrings.getString("brushPromptStep4Text"));
						TTSManager.say(promptStrings.getString("brushPromptStep4Text"), false);
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
						TTSManager.say(promptStrings.getString("brushPromptStep5Text"), false);
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
					TTSManager.say(promptStrings.getString("paintPromptStep2Text"), false);
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
					TTSManager.say(promptStrings.getString("paintPromptStep3Text"), false);
					tempTimer = null;
					rightDrawer.getContainer().removeListener(instance);
					promptStep++;
				}
			} else if (promptStep == 3) {
				if (focalItem != null && focalItem instanceof MoveableItem) {
					if (!((MoveableItem) focalItem).getIsDragged()) {
						paintPrompt.setIcon(promptStrings.getString("paintPromptStep4Icon"));
						paintPrompt.setText(promptStrings.getString("paintPromptStep4Text"));
						TTSManager.say(promptStrings.getString("paintPromptStep4Text"), false);
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
						TTSManager.say(promptStrings.getString("paintPromptStep5Text"), false);
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

	/**
	 * Listen to the events sent by drawers and the application and manage the
	 * prompts accordingly.
	 */
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
		} else if (event.getActionCommand().equals(Application.PAINT_SELECTED) || event.getActionCommand().equals(Application.BRUSH_SELECTED)) {
			if (engagementPromp != null) {
				engagementPromp.dispose();
				TTSManager.stop();
			}
		}
	}
}
