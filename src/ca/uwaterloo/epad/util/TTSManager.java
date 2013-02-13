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

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.apache.log4j.Logger;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

/**
 * This class initialises and manages the Text-To-Speech system
 * (freeTTS).</br>See <a href="http://freetts.sourceforge.net/docs/index.php"
 * >http://freetts.sourceforge.net/docs/index.php</a> for more information about
 * the system.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class TTSManager {
	private static final Logger LOGGER = Logger.getLogger(TTSManager.class);

	// The voice to use
	protected static Voice voice;
	// Indicates that the voice failed to load
	protected static boolean badVoice = false;
	// Chime sound
	protected static Clip chime;
	// Indicates that the chime file failed to load
	protected static boolean badChime = false;

	// Private constructor to prevent instantiation
	private TTSManager() {
	}

	/**
	 * Initialise the Text-To-Speech system: load and allocate the default voice
	 * and also set the speech rate.
	 * 
	 * @see Settings#TTSEnabled
	 * @see Settings#TTSVoice
	 * @see Settings#TTSSpeechRate
	 */
	public static void init() {
		if (!Settings.TTSEnabled)
			return;

		VoiceManager voiceManager = VoiceManager.getInstance();
		voice = voiceManager.getVoice(Settings.TTSVoice);

		if (voice == null) {
			LOGGER.error("TTSManager failed to load voice " + Settings.TTSVoice);
			badVoice = true;
			return;
		}

		voice.allocate();
		voice.setRate(Settings.TTSSpeechRate);

		if (chime == null) {
			String chimeFile = Settings.dataFolder + Settings.chimeFile;
			try {
				chime = AudioSystem.getClip();
				AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(chimeFile));
				chime.open(inputStream);
			} catch (Exception e) {
				LOGGER.error("TTSManager failed to load chime file " + chimeFile);
				badChime = true;
			}
		}
	}

	/**
	 * Say a string if TTS system is enabled and initialised.
	 * 
	 * @param text
	 *            string to say
	 * @param playChime
	 *            play chime sound before synthesising text if <b>true</b>
	 */
	public static void say(String text, boolean playChime) {
		if (!Settings.TTSEnabled)
			return;

		stop();

		if (voice == null && !badVoice) {
			init();
		}

		if (!badVoice)
			new Thread(new TTSRunnable(text, playChime)).start();
	}

	/**
	 * Stop the TTS system.
	 */
	public static void stop() {
		if (!Settings.TTSEnabled)
			return;
		if (voice != null)
			voice.getAudioPlayer().cancel();

		if (chime != null) {
			chime.stop();
			chime.setFramePosition(0);
		}
	}

	/**
	 * Deallocate the resources.
	 */
	public static void dispose() {
		if (!Settings.TTSEnabled)
			return;

		if (voice != null) {
			stop();
			voice.deallocate();
		}
	}

	public static int getChimeDuration() {
		if (chime == null || badChime)
			return -1;

		long d = chime.getMicrosecondLength();

		if (d == AudioSystem.NOT_SPECIFIED)
			return -1;

		return Math.round(d / 1000);
	}

	/**
	 * This class synthesises speech in a separate thread to avoid interrupting
	 * the drawing loop.
	 * 
	 * @author Dmitry Pyryeskin
	 * @version 1.0
	 * 
	 */
	private static class TTSRunnable implements Runnable {
		// String to say
		private String text;
		// Flag to play chime
		private boolean playChime;

		public TTSRunnable(String text, boolean playChime) {
			this.text = text;
			this.playChime = playChime;
		}

		@Override
		public void run() {
			TTSManager.stop();

			if (playChime && !badChime) {
				try {
					chime.start();

					Thread.sleep(getChimeDuration());
				} catch (Exception e) {
					LOGGER.error("Error in Thread.sleep ", e);
				}
			}

			TTSManager.voice.speak(text);
		}
	}
}
