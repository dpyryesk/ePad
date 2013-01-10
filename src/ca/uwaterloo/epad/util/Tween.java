package ca.uwaterloo.epad.util;


public class Tween {
	private long startTime;
	private int tweenLength;
	private float startValue, endValue;
	
	public Tween(int startValue, int endValue, int tweenLength) {
		startTime = System.currentTimeMillis();
		this.tweenLength = tweenLength;
		this.startValue = startValue;
		this.endValue = endValue;
	}
	
	public float getValue() {
		int timePassed = (int) (System.currentTimeMillis() - startTime);
		
		if (timePassed >= tweenLength)
			return endValue;
		else
			return startValue + (endValue - startValue)*((float)timePassed/(float)tweenLength);
	}
}
