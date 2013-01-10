package ca.uwaterloo.epad.util;


public class TTSRunnable implements Runnable {
	private String text;
	
	public TTSRunnable(String text) {
		this.text = text;
	}
	
	@Override
	public void run() {
        // Synthesise speech
		TTSManager.stop();
		TTSManager.voice.speak(text);
	}
}