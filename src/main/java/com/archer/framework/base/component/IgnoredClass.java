package com.archer.framework.base.component;

import java.util.ArrayList;

public class IgnoredClass {
	
	protected static final ArrayList<String> IGNORED = new ArrayList<>(64);
	
	static {
		IGNORED.add("com/archer/net/");
		IGNORED.add("com/archer/tools/");
		IGNORED.add("com/archer/xjson/");
		IGNORED.add("com/archer/math/");
		IGNORED.add("com/archer/log/");
		IGNORED.add("com/google/");
		IGNORED.add("com/mysql/");
		IGNORED.add("META-INF/versions/");
		IGNORED.add("com/zaxxer/");
		IGNORED.add("org/slf4j/");
		IGNORED.add("org/springframework/");
	}
	
	protected static boolean isIgnored(String name) {
		for(String s: IGNORED) {
			if(name.startsWith(s)) {
				return true;
			}
		}
		return false;
	}
	
}
