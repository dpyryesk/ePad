package ca.uwaterloo.epad;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SchemeValidationErrorHandler implements ErrorHandler {
	public int errorCount = 0;
	public int warningCount = 0;
	
	public void fatalError(SAXParseException e) throws SAXException {
		System.err.println("Fatal Error: " + e.getMessage());
		errorCount++;
	}

	public void error(SAXParseException e) throws SAXException {
		System.err.println("Error: " + e.getMessage());
		errorCount++;
	}

	public void warning(SAXParseException e) throws SAXException {
		System.err.println("Warning: " + e.getMessage());
		warningCount++;
	}
}
