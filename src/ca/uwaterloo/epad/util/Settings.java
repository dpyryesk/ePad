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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uwaterloo.epad.xml.XmlAttribute;

public class Settings {
	private final static String settingsFile = "data\\settings.xml";
	
	@XmlAttribute public static Locale locale = new Locale("en");
	
	@XmlAttribute public static int width = 1024;
	@XmlAttribute public static int height = 768;
	@XmlAttribute public static float targetFPS = 60;
	@XmlAttribute public static String dataFolder = "..\\data\\";
	@XmlAttribute public static boolean showPrintDialog = true;

	public static void unmarshallSettings() throws TransformerFactoryConfigurationError, TransformerException, IllegalArgumentException, IllegalAccessException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StreamSource source = new StreamSource(new File(settingsFile));
		DOMResult result = new DOMResult();
		transformer.transform(source, result);
		
		Element root = null;
		Node rootNode = result.getNode().getFirstChild();
		
		if (rootNode instanceof Element) {
			root = (Element)rootNode;
		}
		
		Class<?> c = Settings.class;
		
		for (Field f : c.getFields()) {
			if (f.getAnnotation(XmlAttribute.class) != null) {
				String name = f.getName();
				NodeList list = root.getElementsByTagName(name);
				if (list == null || list.getLength() == 0)
					System.err.println("Settings: no xml value found for field " + name);
				else {
					Node n = list.item(0);
					String strValue = n.getTextContent();
					
					Class<?> type = f.getType();
					
					if (type.equals(String.class)) {
						f.set(null, strValue);
					} else if (type.equals(int.class)) {
						int intValue;
						if (strValue.startsWith("#"))
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
						System.err.println("Settings: field type not supported for " + f.getType() + " " + f.getName());
					}
				}
			}
		}
	}
}
