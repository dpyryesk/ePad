package ca.uwaterloo.epad;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

public class SplashScreen extends Frame {
	private static final long serialVersionUID = -4435437206944198737L;

	private Image img;
	private String text;
	private Font font = new Font("Arial", Font.PLAIN, 16);
	private static SplashScreen instance;

	/**
	 * Positions the window at the centre of the screen, taking into account the
	 * specified width and height
	 */
	private void positionAtCenter(int width, int height) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - width) / 2, (screenSize.height - height) / 2, width, height);
	}

	private SplashScreen(String filename) throws Exception {
		if (instance != null)
			throw (new Exception("SplashScreen is a singleton"));

		instance = this;
		text = "Loading...";

		img = Toolkit.getDefaultToolkit().getImage(filename);
		Image icon = Toolkit.getDefaultToolkit().getImage("data/e.png");
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(img, 0);
		try {
			mt.waitForID(0);
		} catch (InterruptedException e) {
			System.err.println("Unexpected interrupt in waitForID!");
			return;
		}
		if (mt.isErrorID(0)) {
			System.err.println("Couldn't load itemImage file " + filename);
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

	public static void splash(String filename) throws Exception {
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

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		if (img != null)
			g.drawImage(img, 0, 0, this);
		else
			g.clearRect(0, 0, getSize().width, getSize().height);
		
		//g.fillRect(120,140,200,40);
		g.setPaintMode();
		g.setColor(Color.BLACK);
		g.setFont(font);
		g.drawString(text, 30, 100);
	}
}
