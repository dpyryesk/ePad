package ca.uwaterloo.epad;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PShape;
import vialab.SMT.TouchClient;
import vialab.SMT.TouchSource;
import ca.uwaterloo.epad.painting.BristleBrush;
import ca.uwaterloo.epad.painting.Brush;
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
	public String backgroundImage; //"..\\data\\textures\\background_1024x768.png"
	
	// Common graphics
	public static PShape moveIcon, deleteIcon;
	public static PImage paintCan;
	
	// Misc variables
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
		
		// Load common graphics
		moveIcon = loadShape("..\\data\\vector\\move.svg");
		deleteIcon = loadShape("..\\data\\vector\\x.svg");
		paintCan = loadImage("..\\data\\images\\paintCan.png");
		
		if (backgroundImage != null && backgroundImage.length() > 0) {
			bg = this.loadImage(backgroundImage);
		}
		
		TouchClient.setWarnUnimplemented(false);
		client = new TouchClient(this, TouchSource.MOUSE);
		client.setDrawTouchPoints(true, 1);
		
		Button b = new Button(displayWidth - 110, 10, 100, 60, "exit", 16, null, 0);
		client.add(b);
		
		Canvas c = new Canvas(100, 100, 800, 600);
		client.add(c);
		
		RotatingDrawer leftDrawer = RotatingDrawer.makeLeftDrawer(this);
		client.add(leftDrawer);
		
		RotatingDrawer rightDrawer = RotatingDrawer.makeRightDrawer(this);
		client.add(rightDrawer);
		
		leftDrawer.addItem(new Pencil(1));
		leftDrawer.addItem(new Pencil(3));
		leftDrawer.addItem(new Pencil(5));
		leftDrawer.addItem(new Pencil(10));
		leftDrawer.addItem(new BristleBrush(30));
		leftDrawer.addItem(new BristleBrush(50));
		leftDrawer.addItem(new BristleBrush(100));
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
