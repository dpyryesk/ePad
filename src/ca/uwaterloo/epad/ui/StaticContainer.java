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
			fill(transparentColour, transparentAlpha);
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
