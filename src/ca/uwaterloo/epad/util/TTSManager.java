package ca.uwaterloo.epad.util;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TTSManager {
	protected static Voice voice;
	
	private TTSManager(){}
	
	public static void init() {
		VoiceManager voiceManager = VoiceManager.getInstance();
		voice = voiceManager.getVoice("kevin16");

		if (voice == null) {
			System.err.println("Error: TTSManager is unable to load voice kevin16");
			return;
		}

		voice.allocate();
		voice.setRate(120f);
	}

	public static void say(String text) {
		stop();
		
		if (voice == null) {
			init();
		}

		new Thread(new TTSRunnable(text)).start();
	}

	public static void stop() {
		voice.getAudioPlayer().cancel();
	}
}
