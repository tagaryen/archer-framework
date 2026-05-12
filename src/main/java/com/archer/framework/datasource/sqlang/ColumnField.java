package com.archer.framework.datasource.sqlang;

import java.lang.reflect.Field;

public final class ColumnField {
	private Column column;
	private Field field;
	
	public ColumnField(Column column, Field field) {
		this.column = column;
		this.field = field;
	}


	public Column column() {
		return column;
	}
	
	
	public Field field() {
		return field;
	}
}
