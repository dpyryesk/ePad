package ca.uwaterloo.epad.ui;

import java.util.List;

import vialab.SMT.TouchPair;
import vialab.SMT.Zone;

public class ResizeBar extends Zone {
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 2;
	public static final int BOTTOM = 3;
	
	public static final int WIDTH = 30;
	
	private int position;
	private Zone parent;
	
	public ResizeBar(Zone parent, int position) {
		super();
		
		this.parent = parent;
		
		if (position == LEFT) {
			width = WIDTH;
			height = parent.height;
			matrix.translate(-WIDTH, 0);
		}
	}
	
	protected void drawImpl() {
		if (position == LEFT) {
			noStroke();
			fill(0x88333333);
			rect(x, y, width, height);
			
			float middleX = x + width / 2;
			float middleY = y + height / 2;
			
			stroke(128);
			strokeWeight(3);
			
			line(middleX-5, middleY, middleX + 10, middleY-10);
			line(middleX-5, middleY, middleX + 10, middleY+10);
			
			strokeWeight(1);
			
			line(x+7.5f, 25, x+7.5f, middleY-7.5f);
			line(x+7.5f, middleY+7.5f, x+7.5f, height-25);
			
			line(x+15f, 15, x+15f, middleY-15f);
			line(x+15f, middleY+15f, x+15f, height-15);
			
			line(x+22.5f, 25, x+22.5f, middleY-22.5f);
			line(x+22.5f, middleY+22.5f, x+22.5f, height-25);
		}
	}
	
	protected void touchImpl() {
		if (position == LEFT) {
			drag(true, false);
		}
	}
	
	// Override drag
	public void drag(boolean dragX, boolean dragY) {
		if (!getTouchMap().isEmpty()) {
			List<TouchPair> pairs = getTouchPairs(1);
			TouchPair pair = pairs.get(0);
			
			boolean dragLeft = dragX;
			boolean dragRight = dragX;
			boolean dragUp = dragY;
			boolean dragDown = dragY;
			
			if (pair.matches()) {
				lastUpdate = maxTime(pair);
				return;
			}

			int deltaX = 0;
			int deltaY = 0;
			
			if ((dragLeft && pair.to.x < pair.from.x) || (dragRight && pair.to.x > pair.from.x)) {
				deltaX = pair.to.x - pair.from.x;
			}

			if (dragUp && pair.to.y > pair.from.y || dragDown && pair.to.y < pair.from.y) {
				deltaY = pair.to.y - pair.from.y;
			}
			
			if (position == LEFT) {
				parent.translate(deltaX, 0);
				parent.width -= deltaX;
			}
			
			lastUpdate = maxTime(pair);
		}
	}
	
	/**
	 * Performs translate on the current graphics context. Should typically be
	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
	 * 
	 * @param pair
	 *            The TouchPair to drag to/from
	 * @param dragLeft
	 *            Allow dragging left
	 * @param dragRight
	 *            Allow dragging Right
	 * @param dragUp
	 *            Allow dragging Up
	 * @param dragDown
	 *            Allow dragging Down
	 */
	protected void drag(TouchPair pair, boolean dragLeft, boolean dragRight, boolean dragUp, boolean dragDown) {
		
	}
	
	public int getDelta() {
		return (int) (matrix.m03 + WIDTH);
	}
}
