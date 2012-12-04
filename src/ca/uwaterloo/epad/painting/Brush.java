package ca.uwaterloo.epad.painting;

public abstract class Brush {
	public static final int POINT = 0;
	public static final int ELLIPSE = 1;
	public static final int RECTANGLE = 2;
	public static final int TEXTURE = 3;
	
	public int shape;
	public float size;
}
