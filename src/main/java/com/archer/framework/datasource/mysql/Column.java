package com.archer.framework.datasource.mysql;

import java.util.Arrays;

class Column {
	
	private static final char PREX = '_';
	private static final char BE = 'a';
	private static final char ED = 'z';
	
	private String rawName;
	
	private String name;
	
	private String typeName;
	
	private boolean isKey;
	
	public Column(String name) {
		this(name, null, false);
	}
	
	public Column(String name, String typeName, boolean isKey) {
		this.rawName = name;
		this.name = formatColumnName(name);
		this.isKey = isKey;
		this.typeName = typeName;
	}


	public String rawName() {
		return rawName;
	}
	
	public String name() {
		return name;
	}
	
	public void typeName(String typeName) {
		this.typeName = typeName;
	}
	
	public String typeName() {
		return this.typeName;
	}
	
	public void isKey(boolean isKey) {
		this.isKey = isKey;
	}
	
	public boolean isKey() {
		return this.isKey;
	}
	
	public static String formatColumnName(String name) {
		char[] chars = name.toCharArray();
		char[] newChars = new char[chars.length];
		int off = 0, i = 0;
		for(; i < chars.length - 1; i++) {
			if(chars[i] == PREX) {
				if(BE <= chars[i+1] && chars[i+1] <= ED) {
					newChars[off++] = (char) (chars[i+1] - 32);
				} else {
					newChars[off++] = chars[i+1];
				}
				i++;
			} else {
				newChars[off++] = chars[i];
			}
		}
		if(i == chars.length - 1) {
			if(chars[i] != PREX) {
				newChars[off++] = chars[i];
			}
		}
		return new String(Arrays.copyOfRange(newChars, 0, off));
	}
	
	public static String formatFieldName(String name) {
		char[] chars = name.toCharArray();
		char[] newChars = new char[chars.length * 2];
		int off = 0, i = 0;
		for(; i < chars.length; i++) {
			if('A' <= chars[i] && chars[i] <= 'Z') {
				newChars[off++] = '_';
				newChars[off++] = (char) (chars[i] + 32);
			} else {
				newChars[off++] = chars[i];
			}
		}
		return new String(Arrays.copyOfRange(newChars, 0, off));
	}
}
