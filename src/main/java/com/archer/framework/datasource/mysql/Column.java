package com.archer.framework.datasource.mysql;

import java.util.Arrays;

public class Column {
	
	private static final char PREX = '_';
	private static final char BE = 'a';
	private static final char ED = 'z';
	
	private String name;
	
	private int type;
	
	public Column(String name, int type) {
		this.name = formatName(name);
		this.type = type;
	}
	
	private String formatName(String name) {
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
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
