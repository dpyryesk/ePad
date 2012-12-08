import processing.core.PApplet;
import vialab.SMT.TouchClient;
import vialab.SMT.TouchSource;
import vialab.SMT.Zone;

public class SMTTest extends PApplet {
	private static final long serialVersionUID = -6490406521750191092L;

	TouchClient client;

	public void setup() {
		size(1024, 768, P3D);
		frameRate(60);
		client = new TouchClient(this, TouchSource.MOUSE);
		client.setDrawTouchPoints(true);
		SubZone z = new SubZone( 100, 100, 200, 200);
		SuperZone z2 = new SuperZone( 400, 400, 200, 200);
		client.add(z);
		client.add(z2);
	}

	public void draw() {
		background(79, 129, 189);
		fill(0);
		text(round(frameRate) + "fps, # of zones: " + client.getZones().length, width / 2, 10);
	}
	
	private class SuperZone extends Zone {
		public SuperZone(int x, int y, int w, int h) {
			super(x, y, w, h);
		}
		
		protected void drawImpl() {
			rect(0, 0, width, height);
		}
		
		protected void touchImpl() {
			rst();
		}
	}
	
	private class SubZone extends SuperZone {
		public SubZone(int x, int y, int w, int h) {
			super(x, y, w, h);
		}
		
		protected void drawImpl() {
			rect(0, 0, width, height);
		}
	}
}
