package ca.uwaterloo.epad;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import vialab.SMT.TouchClient;
import vialab.SMT.TouchSource;
import ca.uwaterloo.epad.painting.BristleBrush;
import ca.uwaterloo.epad.painting.Brush;
import ca.uwaterloo.epad.painting.Eraser;
import ca.uwaterloo.epad.painting.Paint;
import ca.uwaterloo.epad.painting.Pencil;
import ca.uwaterloo.epad.painting.SpiderBrush;
import ca.uwaterloo.epad.ui.Button;
import ca.uwaterloo.epad.ui.Canvas;
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
	
	// Fields
	public String name;
	public int width = 1024;
	public int height = 768;
	public String layout;
	public int backgroundColor = 0xFFFFFF;
	public String backgroundImage;
	
	// Misc. variables
	private TouchClient client;
	private static boolean hasLoadedParams = false;
	private static PImage bg;
	private static Brush currentBrush;
	private static Paint currentPaint;

	public void setup() {
		loadParameters();
		
		size(width, height, P3D);
		frameRate(60);
		smooth();
		
		if (backgroundImage != null && backgroundImage.length() > 0) {
			bg = this.loadImage(backgroundImage);
		}
		
		TouchClient.setWarnUnimplemented(false);
		client = new TouchClient(this, TouchSource.MOUSE);
		client.setDrawTouchPoints(true, 1);
		
		Button b = new Button(width - 110, 10, 100, 60, "exit", 16, null, 0);
		client.add(b);
		
		Canvas c = new Canvas(100, 100, 800, 600);
		client.add(c);
		
		RotatingDrawer leftDrawer = RotatingDrawer.makeLeftDrawer(this);
		leftDrawer.setColourScheme(0xFF99CC00, 0xFF669900);
		client.add(leftDrawer);
		
		RotatingDrawer rightDrawer = RotatingDrawer.makeRightDrawer(this);
		rightDrawer.setColourScheme(0xFFFFBB33, 0xFFFF8800);
		client.add(rightDrawer);
		
		// test brushes
		Pencil p1 = new Pencil(3);
		p1.setImage("..\\data\\images\\brushes\\pencil1.png");
		leftDrawer.addItem(p1);
		
		BristleBrush b1 = new BristleBrush(60);
		b1.setImage("..\\data\\images\\brushes\\brush1.png");
		leftDrawer.addItem(b1);
		
		/*
		Pencil p2 = new Pencil(20);
		p2.setImage("..\\data\\images\\brushes\\brush2.png");
		leftDrawer.addItem(p2);
		*/
		
		BristleBrush b2 = new BristleBrush(10);
		b2.setImage("..\\data\\images\\brushes\\brush2.png");
		leftDrawer.addItem(b2);
		
		BristleBrush b3 = new BristleBrush(30);
		b3.setImage("..\\data\\images\\brushes\\brush3.png");
		leftDrawer.addItem(b3);
		
		BristleBrush b4 = new BristleBrush(20, 50);
		b4.setImage("..\\data\\images\\brushes\\brush4.png");
		leftDrawer.addItem(b4);
		
		BristleBrush b5 = new BristleBrush(30, 90);
		b5.setImage("..\\data\\images\\brushes\\brush5.png");
		leftDrawer.addItem(b5);
		
		SpiderBrush s = new SpiderBrush();
		s.setImage("..\\data\\images\\brushes\\spider.png");
		leftDrawer.addItem(s);
		
		Eraser e = new Eraser(50, c.backgroundColour);
		e.setImage("..\\data\\images\\brushes\\eraser.png");
		leftDrawer.addItem(e);
		
		leftDrawer.addItem(new Pencil(1));
		leftDrawer.addItem(new Pencil(3));
		leftDrawer.addItem(new Pencil(5));
		leftDrawer.addItem(new Pencil(10));
		leftDrawer.addItem(new BristleBrush(30));
		leftDrawer.addItem(new BristleBrush(50));
		leftDrawer.addItem(new BristleBrush(100));
		leftDrawer.addItem(new BristleBrush(10, 30));
		leftDrawer.addItem(new BristleBrush(30, 50));
		leftDrawer.addItem(new BristleBrush(30, 100));
		leftDrawer.addItem(new SpiderBrush());
		
		rightDrawer.addItem(new Paint(0xFF006884));
		rightDrawer.addItem(new Paint(0xFF00909E));
		rightDrawer.addItem(new Paint(0xFF89DBEC));
		rightDrawer.addItem(new Paint(0xFFED0026));
		rightDrawer.addItem(new Paint(0xFFFA9D00));
		rightDrawer.addItem(new Paint(0xFFFFD08D));
		rightDrawer.addItem(new Paint(0xFFB00051));
		rightDrawer.addItem(new Paint(0xFFF68370));
		rightDrawer.addItem(new Paint(0xFFFEABB9));
		rightDrawer.addItem(new Paint(0xFF6E006C));
		rightDrawer.addItem(new Paint(0xFF91278F));
		rightDrawer.addItem(new Paint(0xFFCF97D7));
		rightDrawer.addItem(new Paint(0xFF000000));
		rightDrawer.addItem(new Paint(0xFF5B5B5B));
		rightDrawer.addItem(new Paint(0xFFD4D4D4));
	}
	
	public void draw() {
		if (SplashScreen.isUp()) SplashScreen.remove();
		
		background(backgroundColor);
	    if (backgroundImage != null && backgroundImage.length() > 0) {
		    imageMode(CORNER);
	    	image(bg, 0, 0, displayWidth, displayHeight);
	    }
		text(Math.round(frameRate) + "fps, # of zones: " + client.getZones().length, 10, 10);
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
	
	public final void keyPressed() {
		if (key == '`') {
			Date now = new Date();
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");
			
			String filename = "data\\screenshot_" + sdt.format(now) + ".png";
			
			PGraphics pg;
			pg = createGraphics(width, height, P3D);
			pg.beginDraw();
			draw();
			client.draw();
			pg.endDraw();
			pg.save(filename);
			
			System.out.println("Screenshot Saved: " + filename);
		}
	}
	
	public static void setPaint(Paint p) {
		if (currentPaint != null)
			currentPaint.deselect();
		currentPaint = p;
		if (currentPaint != null)
			currentPaint.select();
	}
	
	public static Paint getPaint() {
		return currentPaint;
	}
	
	public static void setBrush(Brush b) {
		if (currentBrush != null)
			currentBrush.deselect();
		currentBrush = b;
		if (currentBrush != null)
			currentBrush.select();
	}
	
	public static Brush getBrush() {
		return currentBrush;
	}
}
