import processing.core.PApplet;
import vialab.SMT.Touch;
import vialab.SMT.TouchClient;
import vialab.SMT.TouchSource;
import vialab.SMT.Zone;

public class SMTTest extends PApplet {
	private static final long serialVersionUID = -6490406521750191092L;
	private TouchClient client;
	
	private class testZone extends Zone {
		private boolean flag;
		
		public testZone(int x, int y, boolean flag) {
			super(x, y, 100, 100);
			this.flag = flag;
		}
		
		protected void drawImpl() {
			fill(0x8800ff00);
			rect(0, 0, width, height);
			fill(0);
			if (flag)
				text("original", 10, 10);
			else
				text("copy", 10, 10);
		}
		
		protected void touchDownImpl(Touch touch) {
			if (flag) {
				Zone clone = new testZone(0, 0, false);
				clone.applyMatrix(matrix);
				clone.assign(getTouches());
				//TouchClient.manager.assignTouch(clone, touch);
				unassign(touch);
				client.add(clone);
			}
		}
		
		protected void touchImpl() {
			if (!flag)
				drag();
		}
	}

	public void setup() {
		size(800, 600, P3D);
		frameRate(60);
		smooth();

		client = new TouchClient(this, TouchSource.MOUSE);
		client.setDrawTouchPoints(true);

		testZone z = new testZone(100, 100, true);
		z.rotateAbout(radians(45), CENTER);
		
		client.add(z);
	}

	public void draw() {
		background(79, 129, 189);
		fill(0);
		text(round(frameRate) + "fps, # of zones: " + client.getZones().length, width / 2, 10);
	}
}
