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

public class Timer {
	private long startTime;
	private long timerLength;
	private long timePassed;
	private boolean isPaused;

	public Timer(long length) {
		setTo(length);
	}
	
	public long getTimePassed() {
		if (!isPaused)
			timePassed = System.currentTimeMillis() - startTime;
		return timePassed;
	}
	
	public boolean isTimeOut() {
		return getTimePassed() >= timerLength;
	}
	
	public void pause() {
		isPaused = true;
		timePassed = System.currentTimeMillis() - startTime;
	}
	
	public void resume() {
		isPaused = false;
		startTime = System.currentTimeMillis() - timePassed;
	}
	
	public void restart() {
		setTo(timerLength);
	}
	
	public void setTo(long length) {
		startTime = System.currentTimeMillis();
		timerLength = length;
		isPaused = false;
	}
}
