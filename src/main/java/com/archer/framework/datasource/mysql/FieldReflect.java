package com.archer.framework.datasource.mysql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.archer.framework.datasource.exceptions.SqlException;


class FieldReflect {

	static final String DATE_PATTERN = "yyyy-MM-dd";
	static final String TIME_PATTERN = "HH:mm:ss";
	static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	static final char BRACES_L = '[';
	static final char BRACES_R = ']';
	static final char B_BRACES_L = '{';
	static final char B_BRACES_R = '}';
	static final char COMMA = ',';
	static final char COLON = ':';
	static final char DOT = '.';
	static final char SPACE = ' ';
	static final char ENTER = '\n';
	static final char SINGLE_QUOTE = '\'';
	static final String TAB = "  ";
	static final String SRC_QOUTE = "\"";
	static final String DST_QOUTE = "\\\"";

	static final String BOOL_TYPE = "boolean";
	static final String BYTE_TYPE = "byte";
	static final String CHAR_TYPE = "char";
	static final String SHORT_TYPE = "short";
	static final String INT_TYPE = "int";
	static final String LONG_TYPE = "long";
	static final String FLOAT_TYPE = "float";
	static final String DOUBLE_TYPE = "double";
	
	static final String DECIMAL_PATTERN = "#.00";
	

	static final int DEFAULT_FIELD_COUNT = 24;
	static final int DEFAULT_ARRAY_LEN = 16;
	static final int BASE_MAP_LINE_LENGTH = 256;
	static final int BASE_COLLECTION_LINE_LENGTH = 256;
	
	static String formatObject(Object data) throws SqlException {
		if(data == null) {
			return "null";
		}
		if(data.getClass().isPrimitive()) {
			return formatPrimitive(data);
		}
		if(data instanceof Boolean) {
			return ((Boolean) data?"true":"false");
		}
        if(data instanceof String) {
    		return SINGLE_QUOTE + ((String) data) + SINGLE_QUOTE;
        }
        if(data instanceof Byte) {
            return String.valueOf((Byte)data);
        }	
        if(data instanceof Character) {
            return SINGLE_QUOTE + 
            		String.valueOf((Character)data)
            	+ SINGLE_QUOTE;
        }
        if(data instanceof Short) {
            return String.valueOf((Short)data);
        }
        if(data instanceof Integer) {
            return String.valueOf((Integer)data);
        }
        if(data instanceof Long) {
            return String.valueOf((Long)data);
        }
        if(data instanceof Float) {
            return ((Float)data).toString(); //String.valueOf((Float)data);
        }
        if(data instanceof Double) {
            return String.valueOf((Double)data);
        }
        if(data instanceof BigInteger) {
            return ((BigInteger) data).toString(10);
        }
        if(data instanceof BigDecimal) {
            return ((BigDecimal) data).toPlainString();
        }
        if(data instanceof Number) {
            DecimalFormat df = new DecimalFormat(DECIMAL_PATTERN);
            return df.format(data);
        }
        if(data instanceof Number) {
            DecimalFormat df = new DecimalFormat(DECIMAL_PATTERN);
            return df.format(data);
        }
        if(data instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            try {
            	String ret = sdf.format((Date)data);
        		return SINGLE_QUOTE + ret + SINGLE_QUOTE;
            } catch(Exception ignored) {}
            throw new SqlException("cannot format date "+data);
        }
        if(data instanceof LocalDate) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_PATTERN);
            try {
                String ret = ((LocalDate)data).format(dtf);
        		return SINGLE_QUOTE + ret + SINGLE_QUOTE;
            } catch(Exception ignored) {}
            throw new SqlException("cannot format localDate "+data);
        }
        if(data instanceof LocalTime) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(TIME_PATTERN);
            try {
                String ret = ((LocalTime)data).format(dtf);
        		return SINGLE_QUOTE + ret + SINGLE_QUOTE;
            } catch(Exception ignored) {}
            throw new SqlException("cannot format localTime "+data);
        }
        if(data instanceof LocalDateTime) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
            try {
                String ret = ((LocalDateTime)data).format(dtf);
        		return SINGLE_QUOTE + ret + SINGLE_QUOTE;
            } catch(Exception ignored) {}
            throw new SqlException("cannot format localDateTime "+data);
        }
        throw new SqlException("invalid column type "+data);
	}

	static String formatPrimitive(Object val) 
			throws SqlException {
		Class<?> cls = val.getClass();
		if(BOOL_TYPE.equals(cls.getName())) {
			return String.valueOf((boolean) val);
		} else if(BYTE_TYPE.equals(cls.getName())) {
			return String.valueOf((byte) val);
		} else if(CHAR_TYPE.equals(cls.getName())) {
			return SINGLE_QUOTE+String.valueOf((char) val)+SINGLE_QUOTE;
		} else if(SHORT_TYPE.equals(cls.getName())) {
			return String.valueOf((short) val);
		} else if(INT_TYPE.equals(cls.getName())) {
			return String.valueOf((int) val);
		} else if(LONG_TYPE.equals(cls.getName())) {
			return String.valueOf((long) val);
		} else if(FLOAT_TYPE.equals(cls.getName())) {
			return String.valueOf((float) val);
		} else if(DOUBLE_TYPE.equals(cls.getName())) {
			return String.valueOf((double) val);
		}
		throw new SqlException("unknown primitive type '" 
				+ cls.getName() + "'");
	}
}
