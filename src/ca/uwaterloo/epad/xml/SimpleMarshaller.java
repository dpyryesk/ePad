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

package ca.uwaterloo.epad.xml;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import processing.core.PMatrix3D;
import vialab.SMT.Zone;
import ca.uwaterloo.epad.Application;
import ca.uwaterloo.epad.ui.Canvas;
import ca.uwaterloo.epad.ui.MoveableItem;
import ca.uwaterloo.epad.ui.RotatingDrawer;
import ca.uwaterloo.epad.ui.SlidingDrawer;

/**
 * This class handles saving and loading the workspace layout as well as loading
 * the GUI layout. It handles subclasses of {@link MoveableItem} class
 * automatically, saving and loading the basic parameters (x, y, width, height,
 * drawerId, image, transformation matrix and class name) as well as all extra
 * fields that are marked with {@link XmlAttribute} annotation.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * 
 */
public class SimpleMarshaller {
	private static final Logger LOGGER = Logger.getLogger(SimpleMarshaller.class);

	private static final String NODE_ITEM = "MoveableItem";
	private static final String NODE_CANVAS = "Canvas";
	private static final String NODE_LAYOUT = "Layout";
	private static final String NODE_MATRIX = "matrix";
	private static final String NODE_ROTATING_DRAWER = "RotatingDrawer";
	private static final String NODE_SLIDING_DRAWER = "SlidingDrawer";

	private static final String ATTR_X = "x";
	private static final String ATTR_Y = "y";
	private static final String ATTR_WIDTH = "width";
	private static final String ATTR_HEIGHT = "height";
	private static final String ATTR_DRAWER = "drawerId";
	private static final String ATTR_IMAGE = "image";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_POSITION = "position";
	private static final String ATTR_PRIMARY_COLOUR = "primaryColour";
	private static final String ATTR_SECONDARY_COLOUR = "secondaryColour";
	private static final String ATTR_BACKGROUND_COLOUR = "backgroundColour";
	private static final String ATTR_OVERLAY = "overlay";
	private static final String ATTR_DRAWER_OPEN_WIDTH = "openWidth";
	private static final String ATTR_DRAWER_AUTO_CLOSE = "autoClose";

	private static final String LEFT = "left";
	private static final String RIGHT = "right";
	private static final String TOP = "top";
	@SuppressWarnings("unused")
	private static final String BOTTOM = "bottom";

	/**
	 * Save the layout of the workspace into the specified file.
	 * 
	 * @param file
	 *            a valid file instance to save the layout into
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static void marshallLayout(File file) throws IllegalArgumentException, IllegalAccessException, ParserConfigurationException, TransformerException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element rootElement = document.createElement(NODE_LAYOUT);
		for (Zone z : Application.getChildren())
			marshallItemLayout(z, rootElement);
		document.appendChild(rootElement);

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(file);
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(source, result);
	}

	// Save individual item
	private static void marshallItemLayout(Zone z, Element root) throws IllegalArgumentException, IllegalAccessException {
		// Ignore zones not on top level and drawers
		if (z.getParent() != null)
			return;
		if (z instanceof MoveableItem) {
			Class<? extends Object> c = z.getClass();

			Element child = root.getOwnerDocument().createElement(NODE_ITEM);

			// Save default properties
			child.setAttribute(ATTR_X, Integer.toString(z.x));
			child.setAttribute(ATTR_Y, Integer.toString(z.y));
			child.setAttribute(ATTR_WIDTH, Integer.toString(z.width));
			child.setAttribute(ATTR_HEIGHT, Integer.toString(z.height));
			child.setAttribute(ATTR_DRAWER, Integer.toString(((MoveableItem) z).getDrawerId()));
			child.setAttribute(ATTR_IMAGE, ((MoveableItem) z).getImageFilename());
			child.setAttribute(ATTR_CLASS, c.getName());

			// Save custom fields
			for (Field f : c.getFields()) {
				XmlAttribute attr = f.getAnnotation(XmlAttribute.class);
				if (attr != null) {
					String name = f.getName();
					String value = f.get(z).toString();
					child.setAttribute(name, value);
				}
			}

			// Save transformation matrix
			Element matrix = root.getOwnerDocument().createElement(NODE_MATRIX);
			saveMatrix(z.getGlobalMatrix(), matrix);
			child.appendChild(matrix);

			root.appendChild(child);
		} else if (z instanceof Canvas) {
			Element child = root.getOwnerDocument().createElement(NODE_CANVAS);

			// Save default properties
			child.setAttribute(ATTR_X, Integer.toString(z.x));
			child.setAttribute(ATTR_Y, Integer.toString(z.y));
			child.setAttribute(ATTR_WIDTH, Integer.toString(z.width));
			child.setAttribute(ATTR_HEIGHT, Integer.toString(z.height));
			child.setAttribute(ATTR_BACKGROUND_COLOUR, Integer.toString(((Canvas) z).backgroundColour));
			child.setAttribute(ATTR_OVERLAY, ((Canvas) z).getOverlayImagePath());

			// Save transformation matrix
			Element matrix = root.getOwnerDocument().createElement(NODE_MATRIX);
			saveMatrix(z.getGlobalMatrix(), matrix);
			child.appendChild(matrix);

			root.appendChild(child);
		}
	}

	// Save a transformation matrix
	private static void saveMatrix(PMatrix3D matrix, Element xml) {
		xml.setAttribute("m00", Float.toString(matrix.m00));
		xml.setAttribute("m01", Float.toString(matrix.m01));
		xml.setAttribute("m02", Float.toString(matrix.m02));
		xml.setAttribute("m03", Float.toString(matrix.m03));
		xml.setAttribute("m10", Float.toString(matrix.m10));
		xml.setAttribute("m11", Float.toString(matrix.m11));
		xml.setAttribute("m12", Float.toString(matrix.m12));
		xml.setAttribute("m13", Float.toString(matrix.m13));
		xml.setAttribute("m20", Float.toString(matrix.m20));
		xml.setAttribute("m21", Float.toString(matrix.m21));
		xml.setAttribute("m22", Float.toString(matrix.m22));
		xml.setAttribute("m23", Float.toString(matrix.m23));
		xml.setAttribute("m30", Float.toString(matrix.m30));
		xml.setAttribute("m31", Float.toString(matrix.m31));
		xml.setAttribute("m32", Float.toString(matrix.m32));
		xml.setAttribute("m33", Float.toString(matrix.m33));
	}

	/**
	 * Load the workspace layout from the specified file.
	 * 
	 * @param file
	 *            valid XML file that contains a workspace layout
	 * @throws TransformerException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static void unmarshallLayout(File file) throws TransformerException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StreamSource source = new StreamSource(file);
		DOMResult result = new DOMResult();
		transformer.transform(source, result);

		Node root = result.getNode().getFirstChild();
		if (root == null) {
			LOGGER.error("No data loaded.");
			return;
		}

		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node childNode = children.item(i);
			if (childNode.getNodeName().equals(NODE_ITEM))
				unmarshallItemLayout(childNode);
			else if (childNode.getNodeName().equals(NODE_CANVAS)) {
				NamedNodeMap attributeMap = childNode.getAttributes();
				int x = Integer.parseInt(attributeMap.getNamedItem(ATTR_X).getNodeValue());
				int y = Integer.parseInt(attributeMap.getNamedItem(ATTR_Y).getNodeValue());
				int width = Integer.parseInt(attributeMap.getNamedItem(ATTR_WIDTH).getNodeValue());
				int height = Integer.parseInt(attributeMap.getNamedItem(ATTR_HEIGHT).getNodeValue());
				int backgroundColour = Integer.parseInt(attributeMap.getNamedItem(ATTR_BACKGROUND_COLOUR).getNodeValue());

				Canvas canvas = new Canvas(x, y, width, height, backgroundColour);

				// Load overlay image path
				String overlayPath;
				Node overlayNode = attributeMap.getNamedItem(ATTR_OVERLAY);
				if (overlayNode != null) {
					overlayPath = overlayNode.getNodeValue();
					if (overlayPath != null && overlayPath.length() > 0 && !overlayPath.equals("null"))
						canvas.setOverlayImage(overlayPath);
				}

				// Load transformation matrix
				PMatrix3D matrix = new PMatrix3D();
				NodeList children1 = childNode.getChildNodes();
				for (int j = 0; j < children1.getLength(); j++) {
					childNode = children1.item(j);
					if (childNode.getNodeName().equals(NODE_MATRIX))
						matrix = loadMatrix(childNode);
				}

				canvas.setMatrix(matrix);
				Application.setCanvas(canvas);
			}
		}
	}

	// Load individual item
	private static void unmarshallItemLayout(Node childNode) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		NamedNodeMap attributeMap = childNode.getAttributes();
		if (attributeMap == null || attributeMap.getLength() == 0) {
			LOGGER.error("Failed to unmarshall child, no attributes specified in xml.");
			return;
		}

		// find the name of the child's class
		Node temp = attributeMap.getNamedItem(ATTR_CLASS);
		Class<?> childClass, baseClass;

		if (temp == null) {
			LOGGER.error("Failed to unmarshall child, class attribute not specified in xml.");
			return;
		} else {
			String className = temp.getNodeValue();
			try {
				childClass = Class.forName(className);
			} catch (ClassNotFoundException e) {
				LOGGER.error("Failed to unmarshall child, class " + className + " is not found.");
				return;
			}
		}

		baseClass = childClass;
		while (!baseClass.equals(MoveableItem.class)) {
			baseClass = baseClass.getSuperclass();
			if (baseClass.equals(Object.class)) {
				LOGGER.error("Failed to unmarshall child, class " + childClass.getName() + " is not a subclass of MoveableItem.");
				return;
			}
		}

		// read default properties
		int x = Integer.parseInt(attributeMap.getNamedItem(ATTR_X).getNodeValue());
		int y = Integer.parseInt(attributeMap.getNamedItem(ATTR_Y).getNodeValue());
		int width = Integer.parseInt(attributeMap.getNamedItem(ATTR_WIDTH).getNodeValue());
		int height = Integer.parseInt(attributeMap.getNamedItem(ATTR_HEIGHT).getNodeValue());
		int drawerId = Integer.parseInt(attributeMap.getNamedItem(ATTR_DRAWER).getNodeValue());
		String image = attributeMap.getNamedItem(ATTR_IMAGE).getNodeValue();

		// load transformation matrix
		PMatrix3D matrix = new PMatrix3D();
		NodeList children = childNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals(NODE_MATRIX))
				matrix = loadMatrix(child);
		}

		// create new instance
		MoveableItem item = new MoveableItem(x, y, width, height);
		if (image != null && image.length() > 0)
			item.setImage(image);

		MoveableItem childInstance = (MoveableItem) childClass.getConstructor(MoveableItem.class).newInstance(item);

		// load fields
		for (Field f : childClass.getFields()) {
			if (f.getAnnotation(XmlAttribute.class) != null) {
				String name = f.getName();
				Node n = attributeMap.getNamedItem(name);
				if (n == null) {
					LOGGER.error("No xml value found for field " + name);
				} else {
					setField(f, childInstance, n.getNodeValue());
				}
			}
		}

		childInstance.setMatrix(matrix);
		childInstance.setDrawer(drawerId, false);
		childInstance.addToScreen();
	}

	// Parse a value and set the field
	private static void setField(Field f, Object o, String value) throws IllegalArgumentException, IllegalAccessException, NumberFormatException {
		if (value == null) {
			f.set(o, null);
		} else {
			Class<?> type = f.getType();
			if (type.equals(String.class)) {
				f.set(o, value);
			} else if (type.equals(int.class)) {
				int intValue;
				if (value.startsWith("#"))
					// Handle hexadecimal integers
					intValue = Integer.parseInt(value.substring(1), 16) + 0xFF000000;
				else
					intValue = Integer.parseInt(value);
				f.set(o, intValue);
			} else if (type.equals(float.class)) {
				float floatValue = Float.parseFloat(value);
				f.set(o, floatValue);
			} else if (type.equals(boolean.class)) {
				boolean boolValue = Boolean.parseBoolean(value);
				f.set(o, boolValue);
			} else {
				LOGGER.error("Field type not supported for " + f.getType() + " " + f.getName());
			}
		}
	}

	// Load a transformation matrix
	private static PMatrix3D loadMatrix(Node xml) {
		PMatrix3D matrix = new PMatrix3D();

		NamedNodeMap attributeMap = xml.getAttributes();
		float m00 = Float.parseFloat(attributeMap.getNamedItem("m00").getNodeValue());
		float m01 = Float.parseFloat(attributeMap.getNamedItem("m01").getNodeValue());
		float m02 = Float.parseFloat(attributeMap.getNamedItem("m02").getNodeValue());
		float m03 = Float.parseFloat(attributeMap.getNamedItem("m03").getNodeValue());
		float m10 = Float.parseFloat(attributeMap.getNamedItem("m10").getNodeValue());
		float m11 = Float.parseFloat(attributeMap.getNamedItem("m11").getNodeValue());
		float m12 = Float.parseFloat(attributeMap.getNamedItem("m12").getNodeValue());
		float m13 = Float.parseFloat(attributeMap.getNamedItem("m13").getNodeValue());
		float m20 = Float.parseFloat(attributeMap.getNamedItem("m20").getNodeValue());
		float m21 = Float.parseFloat(attributeMap.getNamedItem("m21").getNodeValue());
		float m22 = Float.parseFloat(attributeMap.getNamedItem("m22").getNodeValue());
		float m23 = Float.parseFloat(attributeMap.getNamedItem("m23").getNodeValue());
		float m30 = Float.parseFloat(attributeMap.getNamedItem("m30").getNodeValue());
		float m31 = Float.parseFloat(attributeMap.getNamedItem("m31").getNodeValue());
		float m32 = Float.parseFloat(attributeMap.getNamedItem("m32").getNodeValue());
		float m33 = Float.parseFloat(attributeMap.getNamedItem("m33").getNodeValue());

		matrix.set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);

		return matrix;
	}

	/**
	 * Load the GUI layout from the specified file.
	 * 
	 * @param app
	 *            Application object
	 * @param file
	 *            valid XML file that contains the GUI layout
	 * @throws TransformerException
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws DOMException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static void unmarshallGui(Application app, File file) throws TransformerException, NumberFormatException, IllegalArgumentException, IllegalAccessException, DOMException,
			InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StreamSource source = new StreamSource(file);
		DOMResult result = new DOMResult();
		transformer.transform(source, result);

		Node root = result.getNode().getFirstChild();
		if (root == null) {
			LOGGER.error("No data loaded.");
			return;
		}

		NamedNodeMap attributeMap = root.getAttributes();

		Class<? extends Object> c = Application.class;

		// Load fields
		for (Field f : c.getFields()) {
			if (f.getAnnotation(XmlAttribute.class) != null) {
				String name = f.getName();
				Node att = attributeMap.getNamedItem(name);
				if (att == null) {
					LOGGER.error("No xml value found for field " + name);
				} else {
					setField(f, app, att.getNodeValue());
				}
			}
		}

		// Process children
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node childNode = children.item(i);
			if (childNode.getNodeName().equals(NODE_ROTATING_DRAWER)) {
				RotatingDrawer drawer = null;
				int drawerId = -1;
				int openWidth = 100;
				boolean autoClose = false;

				attributeMap = childNode.getAttributes();
				String pos = attributeMap.getNamedItem(ATTR_POSITION).getNodeValue();
				int primaryColour = Integer.parseInt(attributeMap.getNamedItem(ATTR_PRIMARY_COLOUR).getNodeValue().substring(1), 16);
				int secondaryColour = Integer.parseInt(attributeMap.getNamedItem(ATTR_SECONDARY_COLOUR).getNodeValue().substring(1), 16);
				if (attributeMap.getNamedItem(ATTR_DRAWER_OPEN_WIDTH) != null)
					openWidth = Integer.parseInt(attributeMap.getNamedItem(ATTR_DRAWER_OPEN_WIDTH).getNodeValue());
				if (attributeMap.getNamedItem(ATTR_DRAWER_AUTO_CLOSE) != null)
					autoClose = Boolean.parseBoolean(attributeMap.getNamedItem(ATTR_DRAWER_AUTO_CLOSE).getNodeValue());

				if (LEFT.equals(pos)) {
					drawer = RotatingDrawer.makeLeftDrawer(app);
					drawerId = Application.LEFT_DRAWER;
				} else if (RIGHT.equals(pos)) {
					drawer = RotatingDrawer.makeRightDrawer(app);
					drawerId = Application.RIGHT_DRAWER;
				} else {
					LOGGER.error("Layout error, only left and right rotating drawers are supported.");
					throw (new DOMException((short) 0, "Layout error"));
				}
				drawer.setColourScheme(primaryColour + 0xFF000000, secondaryColour + 0xFF000000);
				drawer.openWidth = openWidth;
				drawer.autoClose = autoClose;

				Application.setDrawer(drawer, drawerId);
				unmarshallDrawerItems(drawerId, childNode);
			} else if (childNode.getNodeName().equals(NODE_SLIDING_DRAWER)) {
				SlidingDrawer drawer = null;
				int drawerId = -1;
				int openWidth = 100;
				boolean autoClose = false;

				attributeMap = childNode.getAttributes();
				String pos = attributeMap.getNamedItem(ATTR_POSITION).getNodeValue();
				int primaryColour = Integer.parseInt(attributeMap.getNamedItem(ATTR_PRIMARY_COLOUR).getNodeValue().substring(1), 16);
				int secondaryColour = Integer.parseInt(attributeMap.getNamedItem(ATTR_SECONDARY_COLOUR).getNodeValue().substring(1), 16);
				if (attributeMap.getNamedItem(ATTR_DRAWER_OPEN_WIDTH) != null)
					openWidth = Integer.parseInt(attributeMap.getNamedItem(ATTR_DRAWER_OPEN_WIDTH).getNodeValue());
				if (attributeMap.getNamedItem(ATTR_DRAWER_AUTO_CLOSE) != null)
					autoClose = Boolean.parseBoolean(attributeMap.getNamedItem(ATTR_DRAWER_AUTO_CLOSE).getNodeValue());

				// Determine drawer width
				int drawerWidth = app.height / 3;
				Node temp = attributeMap.getNamedItem(ATTR_WIDTH);
				if (temp != null) {
					String value = temp.getNodeValue();
					if (value.endsWith("%")) {
						drawerWidth = (int) (app.height * Float.parseFloat(value.substring(0, value.length() - 1)) / 100);
					} else {
						drawerWidth = Integer.parseInt(value);
					}
				}

				if (TOP.equals(pos)) {
					drawer = SlidingDrawer.makeTopDrawer(app, drawerWidth);
					drawerId = Application.TOP_DRAWER;
				} else {
					LOGGER.error("Layout error, only top sliding drawer is supported");
					throw (new DOMException((short) 0, "Layout error"));
				}

				drawer.setColourScheme(primaryColour + 0xFF000000, secondaryColour + 0xFF000000);
				drawer.openWidth = openWidth;
				drawer.autoClose = autoClose;

				Application.setDrawer(drawer, drawerId);
				unmarshallDrawerItems(drawerId, childNode);
			}
		}
	}

	// Load items in each drawer
	private static void unmarshallDrawerItems(int drawerId, Node xml) throws NumberFormatException, IllegalArgumentException, IllegalAccessException, DOMException, InstantiationException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		NodeList children = xml.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node childNode = children.item(i);
			if (childNode.getNodeName().equals(NODE_ITEM)) {
				NamedNodeMap attributeMap = childNode.getAttributes();
				if (attributeMap == null || attributeMap.getLength() == 0) {
					LOGGER.error("Failed to unmarshall child, no attributes specified in xml.");
					return;
				}

				// Find the name of the child's class
				Node temp = attributeMap.getNamedItem(ATTR_CLASS);
				Class<?> childClass, baseClass;

				if (temp == null) {
					LOGGER.error("Failed to unmarshall child, class attribute not specified in xml.");
					return;
				} else {
					String className = temp.getNodeValue();
					try {
						childClass = Class.forName(className);
					} catch (ClassNotFoundException e) {
						LOGGER.error("Failed to unmarshall child, class " + className + " is not found.");
						return;
					}
				}

				baseClass = childClass;
				while (!baseClass.equals(MoveableItem.class)) {
					baseClass = baseClass.getSuperclass();
					if (baseClass.equals(Object.class)) {
						LOGGER.error("Failed to unmarshall child, class " + childClass.getName() + " is not a subclass of MoveableItem.");
						return;
					}
				}

				// Create a new instance
				MoveableItem item = new MoveableItem(0, 0, 125, 125);

				// Read image path
				temp = attributeMap.getNamedItem(ATTR_IMAGE);
				if (temp != null)
					item.setImage(temp.getNodeValue());

				MoveableItem childInstance = (MoveableItem) childClass.getConstructor(MoveableItem.class).newInstance(item);

				// Load fields
				for (Field f : childClass.getFields()) {
					if (f.getAnnotation(XmlAttribute.class) != null) {
						String name = f.getName();
						Node n = attributeMap.getNamedItem(name);
						if (n == null) {
							LOGGER.error("No xml value found for field " + name);
						} else {
							setField(f, childInstance, n.getNodeValue());
						}
					}
				}

				childInstance.setDrawer(drawerId, true);
				childInstance.doInit();
			}
		}
	}
}
