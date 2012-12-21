package ca.uwaterloo.epad.ui;

import ca.uwaterloo.epad.Application;
import vialab.SMT.Zone;

public class StaticContainer extends Container {
	
	public StaticContainer (int width, int height, Drawer parent) {
		super(0, 0, width, height, parent);
	}

	@Override
	public boolean addItem(Zone item) {
		add(item);
		
		items.put(new Integer(itemCount), item);
		itemCount++;
		
		return true;
	}

	@Override
	protected void drawImpl() {
		if (parent.getPosition() == TOP) {
			noStroke();
			
			rectMode(CORNER);
			fill(backgroundColour);
			rect(0, 0, width, height - 30);
			fill(primaryColour);
			rect(0, 0, width, 10);
		}
	}

	@Override
	protected void pickDrawImpl() {
		rectMode(CORNER);
		rect(0, 0, width, height - 30);
	}

	@Override
	protected void touchImpl() {
		Application.setActionPerformed();
		parent.setActionPerformed();
	}

}
