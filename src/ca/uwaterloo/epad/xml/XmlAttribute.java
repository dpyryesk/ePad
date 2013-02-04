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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used by {@link ca.uwaterloo.epad.xml.SimpleMarshaller
 * SimpleMarshaller} class to determine which fields should be saved and loaded
 * to and from XML layout files. When creating a new widget, make a subclass of
 * {@link ca.uwaterloo.epad.ui.MoveableItem MoveableItem} and mark the important
 * fields that need to be preserved (size as width and height of a brush, etc.)
 * with this annotation, the new subclass will now be handled by
 * {@link ca.uwaterloo.epad.xml.SimpleMarshaller SimpleMarshaller} automatically
 * when the application is saving and loading.
 * 
 * @author Dmitry Pyryeskin
 * @version 1.0
 * @see SimpleMarshaller
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface XmlAttribute {
}
