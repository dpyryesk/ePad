package ca.uwaterloo.epad.painting;

import vialab.SMT.Touch;

public class StrokePoint {
	public float x, y, width, height, xSpeed, ySpeed, motionSpeed, motionAcceleration;
	
	public StrokePoint(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		xSpeed = 0;
		ySpeed = 0;
		motionSpeed = 0;
		motionAcceleration = 0;
	}
	
	public StrokePoint(StrokePoint p) {
		x = p.x;
		y = p.y;
		width = p.width;
		height = p.height;
		xSpeed = p.xSpeed;
		ySpeed = p.ySpeed;
		motionSpeed = p.motionSpeed;
		motionAcceleration = p.motionAcceleration;
	}
	
	public StrokePoint(Touch t) {
		x = t.x;
		y = t.y;
		width = 1;
		height = 1;
		xSpeed = t.xSpeed;
		ySpeed = t.ySpeed;
		motionSpeed = t.motionSpeed;
		motionAcceleration = t.motionAcceleration;
	}
	
	public float dist(StrokePoint p) {
		float dx = x - p.x;
	    float dy = y - p.y;
	    return (float) Math.sqrt(dx*dx + dy*dy);
	}
}
