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

/**
 * Enumerator of application's states. When the application is first stated, it
 * is in IDLE state. When a user starts interacting with it, it switches to
 * RUNNING state. If a user resets the application or it resets automatically
 * after a period of inactivity, it changes the state back to IDLE. Whenever the
 * state is changed from IDLE to RUNNING all timers in PromptManager are reset.
 * Application is switched to PAUSED state when a full-screen dialog is
 * displayed such as Save Dialog or File Browser.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public enum ApplicationState {
	RUNNING, PAUSED, IDLE;
}
