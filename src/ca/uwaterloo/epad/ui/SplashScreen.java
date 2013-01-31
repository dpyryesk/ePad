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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import org.apache.log4j.Logger;

public class SplashScreen extends Frame {
	private static final long serialVersionUID = -4435437206944198737L;
	private static final Logger LOGGER = Logger.getLogger(SplashScreen.class);

	private Image img;
	private String text;
	private Font font = new Font("Arial", Font.PLAIN, 20);
	private static SplashScreen instance;

	/**
	 * Positions the window at the centre of the screen, taking into account the
	 * specified width and height
	 */
	private void positionAtCenter(int width, int height) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - width) / 2, (screenSize.height - height) / 2, width, height);
	}
	
	private SplashScreen() {}

	private SplashScreen(String filename) {
		instance = this;
		text = "";

		img = Toolkit.getDefaultToolkit().getImage(filename);
		Image icon = Toolkit.getDefaultToolkit().getImage("data\\images\\e.png");
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(img, 0);
		try {
			mt.waitForID(0);
		} catch (InterruptedException e) {
			LOGGER.error("Unexpected interrupt in waitForID!");
			return;
		}
		if (mt.isErrorID(0)) {
			LOGGER.error("Failed to load image " + filename);
			return;
		}
		
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		
		if (w < 0) w = 592;
		if (h < 0) h = 533;

		setUndecorated(true);
		setSize(w, h);
		positionAtCenter(w, h);
		setIconImage(icon);
		setAlwaysOnTop(true);
		setVisible(true);
	}

	public static void splash(String filename) {
		if (filename != null && instance == null)
			new SplashScreen(filename);
	}
	
	public static void remove() {
		if (instance != null) {
			instance.dispose();
			instance = null;
		}
	}
	
	public static void setMessage(String text) {
		instance.text = text;
	}
	
	public static boolean isUp() {
		return instance != null;
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		if (img != null)
			g.drawImage(img, 0, 0, this);
		else
			g.clearRect(0, 0, getSize().width, getSize().height);
		
		g.setPaintMode();
		g.setColor(Color.BLACK);
		g.setFont(font);
		g.drawString(text, 30, 100);
	}
}
