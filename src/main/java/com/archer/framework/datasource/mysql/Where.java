package com.archer.framework.datasource.mysql;

public class Where {
	private StringBuilder sql;
	private boolean hasNext;
	
	public Where(String column, Types type, Object val) {
		String strVal = FieldReflect.formatObject(val);
		this.sql = new StringBuilder(" `"+column+"`"+type.typeVal()+strVal);
		this.hasNext = false;
	}

	public Where and(Where next) {
		if(next == null) {
			return this;
		}
		hasNext = true;
		if(next.hasNext) {
			sql.append(" and (" + next.toWhereSql() + ")");
		} else {
			sql.append(" and " + next.toWhereSql());
		}
		return this;
	}
	public Where or(Where next) {
		if(next == null) {
			return this;
		}
		hasNext = true;
		if(next.hasNext) {
			sql.append(" or (" + next.toWhereSql() + ")");
		} else {
			sql.append(" or " + next.toWhereSql());
		}
		return this;
	}
	
	protected String toWhereSql() {
		return sql.toString();
	}

	public enum Types {
		EQ("="),
		SMALLER("<"),
		BIGGER(">"),
		SMALLEREQ("<="),
		BIGGEREQ(">="),
		IN("in");
		
		private String type;
		
		Types(String type) {
			this.type = type;
		}
		
		public String typeVal() {
			return this.type;
		}
	}
}
