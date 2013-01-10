package ca.uwaterloo.epad.util;

public class Timer {
	private long startTime;
	private int timerLength;

	public Timer(int length) {
		startTime = System.currentTimeMillis();
		timerLength = length;
	}
	
	public long getTimePassed() {
		return System.currentTimeMillis() - startTime;
	}
	
	public boolean isTimeOut() {
		return getTimePassed() >= timerLength;
	}
}
