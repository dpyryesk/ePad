package ca.uwaterloo.epad;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import processing.core.PApplet;

public class Launcher {
	// XML names
	private final static String TAG_LAYOUT = "workspaceLayout";

	// Defaults
	private final static String settingsFile = "data\\settings.xml";
	private final static String defaultLayoutFile = "data\\layout.xml";
	private final static String schemaFile = "data\\schema.xsd";

	// State
	public static boolean isInitialized = false;
	
	// Fields
	public static String layoutFile;
	public static Document layoutDocument;
	public static SchemeValidationErrorHandler eh = new SchemeValidationErrorHandler();

	private static void loadSettings() throws SAXException, IOException, ParserConfigurationException {
		// parse an XML document into a DOM tree
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document settingsDocument = parser.parse(new File(settingsFile));

		NodeList nodes = settingsDocument.getElementsByTagName(TAG_LAYOUT);
		if (nodes == null || nodes.getLength() == 0) {
			layoutFile = defaultLayoutFile;
		} else {
			layoutFile = nodes.item(0).getTextContent();
		}

		layoutDocument = parser.parse(new File(layoutFile));
		
		validateLayout(layoutDocument);
		if (eh.errorCount == 0) isInitialized = true;
	}

	public static void validateLayout(Document document) throws ParserConfigurationException, SAXException, IOException {
		// create a SchemaFactory capable of understanding WXS schemas
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// load a WXS schema, represented by a Schema instance
		Source schemaFileStream = new StreamSource(new File(schemaFile));
		Schema schema = factory.newSchema(schemaFileStream);

		// create a Validator instance, which can be used to validate an instance document
		Validator validator = schema.newValidator();
		validator.setErrorHandler(eh);

		// validate the DOM tree
		validator.validate(new DOMSource(document));
	}

	public static void main(String args[]) {
		//splash screen
		try {
			SplashScreen.splash("data/epadLogo.png");
			SplashScreen.setMessage("Loading settings...");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			loadSettings();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		//SplashScreen.setMessage("XML validator found " + eh.errorCount + " errors...");
		SplashScreen.setMessage("Starting...");
		
		// Start the applet
		PApplet.main(new String[] { "--present", "ca.uwaterloo.epad.Application" });
	}
}