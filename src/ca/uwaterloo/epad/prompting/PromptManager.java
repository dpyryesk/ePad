package ca.uwaterloo.epad.prompting;

import java.util.ArrayList;

import processing.core.PApplet;

public class PromptManager {
	private static ArrayList<PromptPopup> activePrompts = new ArrayList<PromptPopup>();
	protected static PApplet parent;
	
	public static void init(PApplet parent) {
		PromptManager.parent = parent;
		
		parent.registerMethod("draw", new PromptManager());
	}
	
	public static void add(PromptPopup pp) {
		activePrompts.add(pp);
	}
	
	public static boolean remove(PromptPopup pp) {
		return activePrompts.remove(pp);
	}
	
	public static void clear() {
		activePrompts.clear();
	}
	
	public static void draw() {
		for (PromptPopup pp : activePrompts) {
			pp.draw();
		}
	}
}
