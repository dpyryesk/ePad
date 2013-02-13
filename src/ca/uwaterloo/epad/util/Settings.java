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
import java.lang.reflect.Field;
import java.util.Locale;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uwaterloo.epad.xml.XmlAttribute;

/**
 * This class contains all of the settings and parameters used by ePad
 * application and it can retrieve them dynamically from a XML file.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class Settings {
	private static final Logger LOGGER = Logger.getLogger(Settings.class);

	/**
	 * Locale of the application, used to select an appropriate resource bundle.
	 */
	@XmlAttribute
	public static Locale locale = new Locale("en");

	/**
	 * Width of the application.
	 */
	@XmlAttribute
	public static int width = 1024;
	/**
	 * Height of the application.
	 */
	@XmlAttribute
	public static int height = 768;
	/**
	 * Target number of frames per second.
	 */
	@XmlAttribute
	public static float targetFPS = 60f;
	/**
	 * Source of the touch events. This may be one of the following:
	 * TUIO_DEVICE, MOUSE, WM_TOUCH, ANDROID or SMART.
	 */
	@XmlAttribute
	public static String touchSourse = "MOUSE";
	/**
	 * Flag indicating whether or not a dialog should be displayed when
	 * attempting to print a painting.
	 */
	@XmlAttribute
	public static boolean showPrintDialog = true;

	// File and folder locations
	/**
	 * Location of the main data folder.
	 */
	@XmlAttribute
	public static String dataFolder = "..\\data\\";
	/**
	 * Location of the save file folder.
	 */
	@XmlAttribute
	public static String saveFolder = "..\\data\\save\\";
	/**
	 * Location of the folder that contains the colouring pages.
	 */
	@XmlAttribute
	public static String colouringFolder = "..\\data\\colouring\\";
	/**
	 * Location of the default GUI layout file.
	 */
	@XmlAttribute
	public static String guiFile = "gui.xml";
	/**
	 * Location of the default workspace layout file.
	 */
	@XmlAttribute
	public static String defaultLayoutFile = "layout_768.xml";

	// TTS settings
	/**
	 * Flag indicating whether or not the Text-To-Speech system should be
	 * enabled.
	 */
	@XmlAttribute
	public static boolean TTSEnabled = true;
	/**
	 * The TTS voice to use.
	 */
	@XmlAttribute
	public static String TTSVoice = "kevin16";
	/**
	 * The rate of speech of the TTS system.
	 */
	@XmlAttribute
	public static float TTSSpeechRate = 120f;
	/**
	 * Path to the sound file with the chime sound.
	 */
	@XmlAttribute
	public static String chimeFile = "sound\\chime.wav";

	// Common GUI settings
	/**
	 * The angle between neighbouring items in a rotating container. Should be
	 * increased for the screens with smaller resolution.
	 */
	@XmlAttribute
	public static int rotatingContainerOffsetAngle = 12;
	/**
	 * The distance between circles of items in a rotating container. Should be
	 * decreased for the screens with smaller resolution.
	 */
	@XmlAttribute
	public static int rotatingContainerOffsetDistance = 60;
	/**
	 * The number of columns of items in a file browser dialog.
	 */
	@XmlAttribute
	public static int fileBrowserColumns = 4;
	/**
	 * The number of rows of items in a file browser dialog.
	 */
	@XmlAttribute
	public static int fileBrowserRows = 2;
	/**
	 * Flag indicating whether or not the debug information should be displayed
	 * on the screen.
	 */
	@XmlAttribute
	public static boolean showDebugInfo = false;

	// Prompt delays
	/**
	 * How long should a prompt remain on the screen (in milliseconds).
	 */
	@XmlAttribute
	public static int promptTTL = 15 * 1000;
	/**
	 * Delay until the setup prompt for the brush drawer is displayed (in
	 * milliseconds).
	 */
	@XmlAttribute
	public static int brushPromptDelay = 60 * 1000;
	/**
	 * Delay until the setup prompt for the paint drawer is displayed (in
	 * milliseconds).
	 */
	@XmlAttribute
	public static int paintPromptDelay = 30 * 1000;
	/**
	 * How long can the application be inactive until the first engagement
	 * prompt is displayed (in milliseconds).
	 */
	@XmlAttribute
	public static int engagementPromptDelay = 60 * 1000;
	/**
	 * How long can the application be inactive until the following engagement
	 * prompt is displayed (in milliseconds).
	 */
	@XmlAttribute
	public static int engagementPromptRepeatDelay = 60 * 1000;
	/**
	 * How long can the application be inactive until the reset prompt is
	 * displayed (in milliseconds).
	 */
	@XmlAttribute
	public static int resetPromptDelay = 5 * 60 * 1000;
	/**
	 * How long should the reset prompt wait until resetting the application
	 * automatically (in milliseconds).
	 */
	@XmlAttribute
	public static int resetDelay = 5 * 60 * 1000;

	/**
	 * Retrieve the settings from the provided XML file.
	 * 
	 * @param filename
	 *            valid path to the XML file with the settings.
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void unmarshallSettings(String filename) throws TransformerFactoryConfigurationError, TransformerException, IllegalArgumentException, IllegalAccessException {
		// Load and parse the source file
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StreamSource source = new StreamSource(new File(filename));
		DOMResult result = new DOMResult();
		transformer.transform(source, result);

		Element root = null;
		Node rootNode = result.getNode().getFirstChild();

		if (rootNode instanceof Element) {
			root = (Element) rootNode;
		}

		Class<?> c = Settings.class;

		// Attempt to find and parse a value for each field that is marked with
		// @XmlAttribute annotation
		for (Field f : c.getFields()) {
			if (f.getAnnotation(XmlAttribute.class) != null) {
				String name = f.getName();
				NodeList list = root.getElementsByTagName(name);
				if (list == null || list.getLength() == 0)
					LOGGER.error("No xml value found for field " + name);
				else {
					Node n = list.item(0);
					String strValue = n.getTextContent();

					Class<?> type = f.getType();

					if (type.equals(String.class)) {
						f.set(null, strValue);
					} else if (type.equals(int.class)) {
						int intValue;
						if (strValue.startsWith("#"))
							// Handle hexadecimal integers marked with '#'
							intValue = Integer.parseInt(strValue.substring(1), 16) + 0xFF000000;
						else
							intValue = Integer.parseInt(strValue);
						f.set(null, intValue);
					} else if (type.equals(float.class)) {
						float floatValue = Float.parseFloat(strValue);
						f.set(null, floatValue);
					} else if (type.equals(boolean.class)) {
						boolean boolValue = Boolean.parseBoolean(strValue);
						f.set(null, boolValue);
					} else if (type.equals(Locale.class)) {
						Locale l = new Locale(strValue);
						f.set(null, l);
					} else {
						LOGGER.error("Field type not supported for " + f.getType() + " " + f.getName());
					}
				}
			}
		}
	}
}
