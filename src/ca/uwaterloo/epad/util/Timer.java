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

package ca.uwaterloo.epad.util;

/**
 * This class implements a simple timer that returns a value on demand.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class Timer {
	// When was the timer started
	private long startTime;
	// How many millisecond was the timer set for
	private long timerLength;
	// How long was the timer running
	private long timePassed;
	// Is the timer currently paused
	private boolean isPaused;

	/**
	 * Create a timer and set it to the number of milliseconds provided.
	 * 
	 * @param length
	 *            the number of millisecond the timer should be set for
	 */
	public Timer(long length) {
		setTo(length);
	}

	/**
	 * Calculate how long the timer was running.
	 * 
	 * @return the number of milliseconds the timer was running
	 */
	public long getTimePassed() {
		if (!isPaused)
			timePassed = System.currentTimeMillis() - startTime;
		return timePassed;
	}

	/**
	 * Calculate if the timer has ran the amount of time it was set to run.
	 * 
	 * @return <b>true</b> if the timer has ran the amount of time it was set to
	 *         run and <b>false</b> if it is still running
	 */
	public boolean isTimeOut() {
		return getTimePassed() >= timerLength;
	}

	/**
	 * Pause the timer.
	 */
	public void pause() {
		isPaused = true;
		timePassed = System.currentTimeMillis() - startTime;
	}

	/**
	 * Resume the timer.
	 */
	public void resume() {
		isPaused = false;
		startTime = System.currentTimeMillis() - timePassed;
	}

	/**
	 * Restart the timer.
	 */
	public void restart() {
		setTo(timerLength);
	}

	/**
	 * Set the number of milliseconds the timer should run starting from now.
	 * 
	 * @param length
	 *            the number of millisecond the timer should be set for
	 */
	public void setTo(long length) {
		startTime = System.currentTimeMillis();
		timerLength = length;
		isPaused = false;
	}
}
