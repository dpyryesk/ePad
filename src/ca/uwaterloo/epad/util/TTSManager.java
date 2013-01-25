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

package ca.uwaterloo.epad.util;

import org.apache.log4j.Logger;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TTSManager {
	private static final Logger LOGGER = Logger.getLogger(TTSManager.class);
	
	protected static Voice voice;
	protected static boolean badVoice = false;
	
	private TTSManager(){}
	
	public static void init() {
		if (!Settings.TTSEnabled) return;
		
		VoiceManager voiceManager = VoiceManager.getInstance();
		voice = voiceManager.getVoice(Settings.TTSVoice);

		if (voice == null) {
			LOGGER.error("TTSManager failed to load voice " + Settings.TTSVoice);
			badVoice = true;
			return;
		}

		voice.allocate();
		voice.setRate(Settings.TTSSpeechRate);
	}

	public static void say(String text) {
		if (!Settings.TTSEnabled) return;
		
		stop();
		
		if (voice == null && !badVoice) {
			init();
		}

		if (!badVoice)
			new Thread(new TTSRunnable(text)).start();
	}

	public static void stop() {
		if (!Settings.TTSEnabled) return;
		if (voice != null)
			voice.getAudioPlayer().cancel();
	}
	
	public static void dispose() {
		if (!Settings.TTSEnabled) return;
		
		if (voice != null) {
			stop();
			voice.deallocate();
		}
	}
}
