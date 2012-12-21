package ca.uwaterloo.epad.ui;

import java.util.Date;

public class Tween {
	private long startTime;
	private int tweenLength;
	private float startValue, endValue;
	
	public Tween(int startValue, int endValue, int tweenLength) {
		startTime = new Date().getTime();
		this.tweenLength = tweenLength;
		this.startValue = startValue;
		this.endValue = endValue;
	}
	
	public float getValue() {
		int timePassed = (int) (new Date().getTime() - startTime);
		
		if (timePassed >= tweenLength)
			return endValue;
		else
			return startValue + (endValue - startValue)*((float)timePassed/(float)tweenLength);
	}
}
