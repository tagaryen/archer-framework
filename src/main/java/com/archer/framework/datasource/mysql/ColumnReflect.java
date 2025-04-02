package com.archer.framework.datasource.mysql;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.archer.tools.java.ClassUtil;

class ColumnReflect {

	static final String BOOL_TYPE = "boolean";
	static final String BYTE_TYPE = "byte";
	static final String CHAR_TYPE = "char";
	static final String SHORT_TYPE = "short";
	static final String INT_TYPE = "int";
	static final String LONG_TYPE = "long";
	static final String FLOAT_TYPE = "float";
	static final String DOUBLE_TYPE = "double";
	
	static final char GENERIC_L = '<';
	static final char GENERIC_R = '>';
	static final String COMMA = ",";

	static final DateFormat DEFAULT_DATE_FORMAT = 
			new SimpleDateFormat("yyyy-MM-dd");
	static final DateTimeFormatter DEFAULT_LOCAL_DATE_FORMAT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd");
	static final DateTimeFormatter DEFAULT_LOCAL_TIME_FORMAT =
			DateTimeFormatter.ofPattern("HH:mm:ss");
	static final DateTimeFormatter DEFAULT_LOCAL_DATE_TIME_FORMAT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	static Map<Class<?>, Map<String, Field>> classFieldMap = new ConcurrentHashMap<>();

	
	protected ColumnReflect() {}

	@SuppressWarnings("unchecked")
	public <T> T newInstanceAndSetColumns(Column[] colums, ResultSet rs, Class<T> cls) throws SQLException {
		T ins = (T) ClassUtil.newInstance(cls);
		Map<String, Field> fieldMap = getClassFieldMap(cls);
		for(int i = 1; i < colums.length; i++) {
			Field f = fieldMap.getOrDefault(colums[i].name(), null);
			if(f == null) {
				throw new SQLException("can not set " + colums[i].name() + 
						" to class " + cls.getName());
			}
			reflectToField(f, ins, rs.getString(i));
		}
		return ins;
	}
	
	public void reflectToField(Field f, Object classObj, String val) 
			throws SQLException {
		Class<?> cls = f.getType();
		if(cls.isPrimitive()) {
			reflectToPrimitive(cls, f, classObj, val);
		} else {
			reflectToPopularClass(cls, f, classObj, val);
		}
	}
	
	
	static void reflectToPrimitive(Class<?> cls, Field f, Object classObj, String val) 
			throws SQLException {
		try {
			if(BOOL_TYPE.equals(cls.getName())) {
				f.set(classObj, Boolean.valueOf(val).booleanValue());
			} else if(BYTE_TYPE.equals(cls.getName())) {
				f.set(classObj, Integer.valueOf(val).byteValue());
			} else if(CHAR_TYPE.equals(cls.getName())) {
				f.set(classObj, Integer.valueOf(val).byteValue());
			} else if(SHORT_TYPE.equals(cls.getName())) {
				f.set(classObj, Integer.valueOf(val).shortValue());
			} else if(INT_TYPE.equals(cls.getName())) {
				f.set(classObj, Integer.valueOf(val).intValue());
			} else if(LONG_TYPE.equals(cls.getName())) {
				f.set(classObj, Long.valueOf(val).longValue());
			} else if(FLOAT_TYPE.equals(cls.getName())) {
				f.set(classObj, Float.valueOf(val).floatValue());
			} else if(DOUBLE_TYPE.equals(cls.getName())) {
				f.set(classObj, Double.valueOf(val).doubleValue());
			}
		} catch(Exception e) {
			throw new SQLException("can not set  '" + val + "' to "
					+ classObj.getClass().getName() + "." + f.getName());
		}
		throw new SQLException("unknown primitive type '" 
				+ cls.getName() + "'");
	}
	
	void reflectToPopularClass(Class<?> cls, Field f, Object classObj, String val) 
			throws SQLException {
		try {
			if(cls.equals(String.class)) {
				f.set(classObj, val);
			} else if(cls.equals(Byte.class)) {
				f.set(classObj, Byte.valueOf(val));
			} else if(cls.equals(Character.class)) {
				f.set(classObj, val.charAt(0));
			} else if(cls.equals(Integer.class)) {
				f.set(classObj, Integer.valueOf(val));
			} else if(cls.equals(Long.class)) {
				f.set(classObj, Long.valueOf(val));
			} else if(cls.equals(Float.class)) {
				f.set(classObj, Float.valueOf(val));
			} else if(cls.equals(Double.class)) {
				f.set(classObj, Double.valueOf(val));
			} else if(Date.class.isAssignableFrom(cls)) {
				try {
					f.set(classObj, DEFAULT_DATE_FORMAT.parse(val));
				} catch(Exception ignore) {
					throw new SQLException("can not parse '"+
							val+"' to Date");
				}
			} else if(cls.equals(LocalDate.class)) {
				try {
					f.set(classObj, LocalDate.parse(val, DEFAULT_LOCAL_DATE_FORMAT));
				} catch(Exception ignore) {
					throw new SQLException("can not parse '"+
							(val)+"' to LocalDate");
				}
			} else if(cls.equals(LocalTime.class)) {
				try {
					f.set(classObj, LocalTime.parse(val, DEFAULT_LOCAL_TIME_FORMAT));
				} catch(Exception ignore) {
					throw new SQLException("can not parse '"+
							(val)+"' to LocalTime");
				}
			} else if(cls.equals(LocalDateTime.class)) {
				try {
					f.set(classObj, LocalDateTime.parse(val, DEFAULT_LOCAL_DATE_TIME_FORMAT));
				} catch(Exception ignore) {
					throw new SQLException("can not parse '"+
							(val)+"' to LocalDateTime");
				}
			} else if(cls.equals(BigInteger.class)) {
				try {
					f.set(classObj, new BigInteger(val));
				} catch(Exception ignore) {
					throw new SQLException("can not parse '"+
							(val)+"' to BigInteger");
				}
			} else if(cls.equals(BigDecimal.class)) {
				try {
					f.set(classObj, new BigDecimal(val));
				} catch(Exception ignore) {
					throw new SQLException("can not parse '"+
							(val)+"' to BigDecimal");
				}
			} else {
				throw new SQLException("unknown type '"+
						(cls.getName())+"'");
			}
		} catch(SQLException e) {
			throw e;
		} catch(Exception e) {
			throw new SQLException("can not set  '" + val + "' to "
					+ classObj.getClass().getName() + "." + f.getName());
		}
	}
	
	public String formatEntityField(Field f, Object entityObj) throws SQLException {
		Class<?> cls = f.getType();
		if(cls.isPrimitive()) {
			return formatPrimitive(cls, f, entityObj);
		} else {
			return formatPopularClass(cls, f, entityObj);
		}
		
	}
	
	static String formatPrimitive(Class<?> cls, Field f, Object classObj) 
			throws SQLException {
		try {
			if(BOOL_TYPE.equals(cls.getName()) || BYTE_TYPE.equals(cls.getName()) || 
				CHAR_TYPE.equals(cls.getName()) || SHORT_TYPE.equals(cls.getName()) || 
				INT_TYPE.equals(cls.getName()) || LONG_TYPE.equals(cls.getName()) || 
				FLOAT_TYPE.equals(cls.getName()) || DOUBLE_TYPE.equals(cls.getName())) {
				return String.valueOf(f.get(classObj));
			}
		} catch(Exception e) {
			throw new SQLException("can not get "
					+ classObj.getClass().getName() + "." + f.getName());
		}
		throw new SQLException("unknown primitive type '" 
				+ cls.getName() + "'");
	}
	
	String formatPopularClass(Class<?> cls, Field f, Object classObj) 
			throws SQLException {
		try {
			if(cls.equals(String.class)) {
				return "'" + (String) f.get(classObj) + "'";
			} else if(cls.equals(Byte.class) || cls.equals(Character.class)) {
				return String.valueOf(((Byte)f.get(classObj)).byteValue());
			} else if(cls.equals(Integer.class)) {
				return String.valueOf(((Integer)f.get(classObj)).intValue());
			} else if(cls.equals(Long.class)) {
				return String.valueOf(((Long)f.get(classObj)).longValue());
			} else if(cls.equals(Float.class)) {
				return String.valueOf(((Float)f.get(classObj)).floatValue());
			} else if(cls.equals(Double.class)) {
				return String.valueOf(((Double)f.get(classObj)).doubleValue());
			} else if(Date.class.isAssignableFrom(cls)) {
				return "'" + DEFAULT_DATE_FORMAT.format((Date)f.get(classObj)) + "'";
			} else if(cls.equals(LocalDate.class)) {
				return "'" + DEFAULT_LOCAL_DATE_FORMAT.format((LocalDate)f.get(classObj)) + "'";
			} else if(cls.equals(LocalTime.class)) {
				return "'" + DEFAULT_LOCAL_TIME_FORMAT.format((LocalTime)f.get(classObj)) + "'";
			} else if(cls.equals(LocalDateTime.class)) {
				return "'" + DEFAULT_LOCAL_DATE_TIME_FORMAT.format((LocalDateTime)f.get(classObj)) + "'";
			} else if(cls.equals(BigInteger.class)) {
				return ((BigInteger)f.get(classObj)).toString(10);
			} else if(cls.equals(BigDecimal.class)) {
				return ((BigDecimal)f.get(classObj)).toPlainString();
			} else {
				throw new SQLException("unknown type '"+
						(cls.getName())+"'");
			}
		} catch(Exception e) {
			throw new SQLException("can not format "
					+ classObj.getClass().getName() + "." + f.getName() + " to String");
		}
	}
	
	private Map<String, Field> getClassFieldMap(Class<?> cls) {
		Map<String, Field> fieldMap = classFieldMap.getOrDefault(cls, null);
		if(fieldMap == null) {
			Field[] field0s = cls.getFields();
			Field[] field1s = cls.getDeclaredFields();
			Field[] fields = new Field[field0s.length + field1s.length];
			System.arraycopy(field0s, 0, fields, 0, field0s.length);
			System.arraycopy(field1s, 0, fields, field0s.length, field1s.length);
			fieldMap = new TreeMap<>();
			for(Field f: fields) {
				f.setAccessible(true);
				fieldMap.put(f.getName(), f);
			}
			classFieldMap.put(cls, fieldMap);
		}
		return fieldMap;
	}
}
