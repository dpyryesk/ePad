package ca.uwaterloo.epad.xml;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface XmlAttribute {
	public static final String NULL = "[unassigned]";
	
	String name() default NULL;
	String defaultValue() default NULL;
}
