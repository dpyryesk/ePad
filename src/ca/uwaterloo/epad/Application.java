package ca.uwaterloo.epad;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import vialab.SMT.TouchClient;
import vialab.SMT.TouchSource;
import ca.uwaterloo.epad.ui.Button;
import ca.uwaterloo.epad.ui.Canvas;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.ui.RotatingDrawer;

public class Application extends PApplet {
	private static final long serialVersionUID = -1354251777507926593L;
	
	// XML Names
	private static final String NODE_NAME = "Application";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_WIDTH = "width";
	private static final String ATTR_HEIGHT = "height";
	private static final String ATTR_FRAMERATE = "frameRate";
	private static final String ATTR_LAYOUT = "layout";
	private static final String ATTR_BACKGROUNDCOLOR = "backgroundColor";
	private static final String ATTR_BACKGROUNDIMAGE = "backgroundImage";
	
	// Common elements
	public static PShape moveIcon, deleteIcon;
	
	// Fields
	public String name;
	public int width = 1024;
	public int height = 768;
	public String layout;
	public int backgroundColor = 0xFFFFFF;
	public String backgroundImage; //"..\\data\\background_1024x768.png"
	
	private TouchClient client;
	
	private boolean hasLoadedParams = false;
	private static PImage bg;
	
	private Canvas c;
	private RotatingDrawer leftDrawer;
	private RotatingDrawer rightDrawer;

	public void setup() {
		loadParameters();
		
		size(width, height, P3D);
		frameRate(60);
		smooth();
		
		// Load common graphics
		moveIcon = loadShape("..\\data\\vector\\move.svg");
		deleteIcon = loadShape("..\\data\\vector\\x.svg");
		
		if (backgroundImage != null && backgroundImage.length() > 0) {
			bg = this.loadImage(backgroundImage);
		}
		
		TouchClient.setWarnUnimplemented(false);
		client = new TouchClient(this, TouchSource.MOUSE);
		client.setDrawTouchPoints(true);
		
		Button b = new Button(displayWidth - 110, 10, 100, 60, "exit", 16, null, 0);
		client.add(b);
		
		c = new Canvas(300, 100, 800, 600);
		client.add(c);
		
		leftDrawer = RotatingDrawer.makeLeftDrawer(this);
		client.add(leftDrawer);
		
		rightDrawer = RotatingDrawer.makeRightDrawer(this);
		client.add(rightDrawer);
		
		// add sample items to drawers
		leftDrawer.addItem(new MoveableItem("1", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("2", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("3", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("4", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("5", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("6", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("7", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("8", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("9", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("10", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("11", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("12", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("13", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("14", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("15", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("16", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("17", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("18", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("19", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("20", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("21", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("22", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("23", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("24", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("25", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("26", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("27", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("28", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("29", 0, 0, 150, 150));
		leftDrawer.addItem(new MoveableItem("30", 0, 0, 150, 150));
		
		rightDrawer.addItem(new MoveableItem("1", 0, 0, 150, 150));
		rightDrawer.addItem(new MoveableItem("2", 0, 0, 150, 150));
		rightDrawer.addItem(new MoveableItem("3", 0, 0, 150, 150));
		rightDrawer.addItem(new MoveableItem("4", 0, 0, 150, 150));
		rightDrawer.addItem(new MoveableItem("5", 0, 0, 150, 150));
		rightDrawer.addItem(new MoveableItem("6", 0, 0, 150, 150));
		rightDrawer.addItem(new MoveableItem("7", 0, 0, 150, 150));
		rightDrawer.addItem(new MoveableItem("8", 0, 0, 150, 150));
		rightDrawer.addItem(new MoveableItem("9", 0, 0, 150, 150));
		rightDrawer.addItem(new MoveableItem("10", 0, 0, 150, 150));
	}
	
	public void draw() {
		if (SplashScreen.isUp()) SplashScreen.remove();
		
		background(backgroundColor);
	    if (backgroundImage != null && backgroundImage.length() > 0) {
		    imageMode(CORNER);
	    	//SMTUtilities.aspectImage(this, bg, 0, 0, displayWidth, displayHeight);
	    	image(bg, 0, 0, displayWidth, displayHeight);
	    }
		text(Math.round(frameRate) + "fps, # of zones: " + client.getZones().length, width / 2, 10);
	}
	
	private void loadParameters () {
		if (hasLoadedParams) return;
		if (Launcher.isInitialized) {
			NodeList list = Launcher.layoutDocument.getElementsByTagName(NODE_NAME);
			if (list != null && list.getLength() != 0) {
				Node node = list.item(0);
				NamedNodeMap map = node.getAttributes();
				
				Node attr = map.getNamedItem(ATTR_NAME);
				if (attr != null) name = attr.getNodeValue();
				
				attr = map.getNamedItem(ATTR_WIDTH);
				if (attr != null) width = Integer.parseInt(attr.getNodeValue());
				if (width < 0) width = displayWidth;
				
				attr = map.getNamedItem(ATTR_HEIGHT);
				if (attr != null) height = Integer.parseInt(attr.getNodeValue());
				if (height < 0) height = displayHeight;
				
				attr = map.getNamedItem(ATTR_FRAMERATE);
				if (attr != null) frameRate = Float.parseFloat(attr.getNodeValue());
				
				attr = map.getNamedItem(ATTR_LAYOUT);
				if (attr != null) layout = attr.getNodeValue();
				
				attr = map.getNamedItem(ATTR_BACKGROUNDCOLOR);
				if (attr != null) backgroundColor = Integer.parseInt(attr.getNodeValue(), 16);
				
				attr = map.getNamedItem(ATTR_BACKGROUNDIMAGE);
				if (attr != null) backgroundImage = attr.getNodeValue();
			}
			
			hasLoadedParams = true;
		}
	}
	
	/*
	public final void keyPressed() {
		if (key == '`') {
			Date now = new Date();
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");
			
			String filename = "data\\screenshot_" + sdt.format(now) + ".png";
			
			PGraphics pg;
			pg = createGraphics(width, height, P3D);
			pg.beginDraw();
			pg.image(g.get(), 0, 0);
			pg.endDraw();
			pg.save(filename);
			
			System.out.println("Screenshot Saved: " + filename);
		}
	}
	*/
}
