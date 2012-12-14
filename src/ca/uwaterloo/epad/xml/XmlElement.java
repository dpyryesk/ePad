package ca.uwaterloo.epad.xml;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface XmlElement { //@XmlElement
	String name();
}
