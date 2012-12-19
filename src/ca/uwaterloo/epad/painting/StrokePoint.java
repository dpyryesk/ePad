package ca.uwaterloo.epad.painting;


public class StrokePoint {
	public float x, y, width, height, xSpeed, ySpeed, motionSpeed, motionAcceleration;
	
	public StrokePoint(float x, float y, float width, float height, float xSpeed, float ySpeed, float motionSpeed, float motionAcceleration) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.xSpeed = xSpeed;
		this.ySpeed = ySpeed;
		this.motionSpeed = motionSpeed;
		this.motionAcceleration = motionAcceleration;
	}
	
	public float dist(StrokePoint p) {
		float dx = x - p.x;
	    float dy = y - p.y;
	    return (float) Math.sqrt(dx*dx + dy*dy);
	}
}
