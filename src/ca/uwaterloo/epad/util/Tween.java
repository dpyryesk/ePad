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
 * This class provides simple tweening functionality useful for time-based
 * animation.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class Tween {
	// When the tween was started
	private long startTime;
	// How long should the tween last, in milliseconds
	private int tweenLength;
	// Starting and ending value of the variable to tween
	private float startValue, endValue;

	/**
	 * Create and start a tween.
	 * 
	 * @param startValue
	 *            the initial value
	 * @param endValue
	 *            the final value
	 * @param tweenLength
	 *            the length of the tween
	 */
	public Tween(int startValue, int endValue, int tweenLength) {
		startTime = System.currentTimeMillis();
		this.tweenLength = tweenLength;
		this.startValue = startValue;
		this.endValue = endValue;
	}

	/**
	 * Calculate the time-based tween value. For example, if you would like to
	 * tween a variable between 0 and 100 in 1000 milliseconds, than calling
	 * this method after 500 milliseconds will return 50. If you call this
	 * method after 2000 milliseconds, the final value (100) will be returned.
	 * 
	 * @return the tweened value based on the current time
	 */
	public float getValue() {
		int timePassed = (int) (System.currentTimeMillis() - startTime);

		if (timePassed >= tweenLength)
			return endValue;
		else
			return startValue + (endValue - startValue) * ((float) timePassed / (float) tweenLength);
	}
}
