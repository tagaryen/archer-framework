package com.archer.framework.datasource.mysql;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import com.archer.framework.base.annotation.Inject;
import com.archer.framework.datasource.annotation.Entity;
import com.archer.framework.datasource.exceptions.SqlException;

public abstract class Repository<T> {

	private Class<T> cls = null;
	
	private Field[] fs = null;
	
	private ColumnField[] cfs = null;
	
	private ColumnField key = null;
	
	private String table = null;
	
	@Inject
	MySQLExecutor exe;
	
	public T save(T ins) {
		try {
			initTable();
			Object keyV = key.field().get(ins);
			if(keyV == null) {
				insertSave(ins, null);
				T newIns = exe.queryOne("select max(`" + key.column().rawName() + "`) as `"+key.column().rawName()+"` from `" + table + "`", cls);
				key.field().set(ins, key.field().get(newIns));
				return ins;
			}
			String strV = FieldReflect.formatObject(keyV);
			T exists = exe.queryOne("select `" + key.column().rawName() + "` from `" + table + "` where `" + key.column().rawName() + "` = " + strV, cls);
			if(exists == null) {
				insertSave(ins, strV);
			} else {
				updateSave(ins, strV);
			}
			return ins;
		} catch(SQLException e) {
			throw new SqlException(e);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public T findOneBy(Where where) {
		initTable();
		try {
			return exe.queryOne("select * from `" + table + "` where " + where.toWhereSql(), cls);
		} catch (SQLException e) {
			throw new SqlException(e);
		}
	}
	
	public List<T> findListBy(Where where) {
		initTable();
		try {
			return exe.query("select * from `" + table + "` where " + where.toWhereSql(), cls);
		} catch (SQLException e) {
			throw new SqlException(e);
		}
	}
	
	private void initTable() {
		if(key == null) {
			Class<T> cls = getEntityClass();
			Entity entity = cls.getAnnotation(Entity.class);
			if(entity == null) {
				table = Column.formatFieldName(cls.getSimpleName());
			}
			table = entity.tableName();
			try {
				List<Column> columns = exe.showColumns(table);
				fs = loadEntityFields(cls);
				HashMap<String, Field> fsMap = new HashMap<>();
				for(Field f: fs) {
					f.setAccessible(true);
					fsMap.put(f.getName(), f);
				}
				cfs = new ColumnField[columns.size()];
				int i = 0;
				for(Column col: columns) {
					if(!fsMap.containsKey(col.name())) {
						throw new SqlException("column " + col.rawName() + " can not be found in " + cls.getName());
					}
					Field f = fsMap.get(col.name());
					cfs[i++] = new ColumnField(col, f);
					
					if(col.isKey()) {
						if(key != null && "id".equals(key.column().name())) {
							continue;
						}
						key = cfs[i-1];
					}
				}
			} catch (SQLException e) {
				throw new SqlException(e);
			}
		}
		if(key == null) {
			throw new SqlException("can not found primary key in " + table);
		}
		
	}
	
	

	@SuppressWarnings({"unchecked" })
	private Class<T> getEntityClass() {
		if(cls == null) {
			Type[] types = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
			cls =  (Class<T>) types[0];
		}
		return cls;
	}
	
	private Field[] loadEntityFields(Class<?> cls) {
		Field[] selfFs = cls.getDeclaredFields();
		if(cls.getSuperclass().equals(Object.class)) {
			return selfFs;
		} else {
			Field[] superFs = loadEntityFields(cls.getSuperclass());
			Field[] newfs = new Field[selfFs.length + superFs.length];
			System.arraycopy(selfFs, 0, newfs, 0, selfFs.length);
			System.arraycopy(superFs, 0, newfs, selfFs.length, superFs.length);
			selfFs = newfs;
		}
		return selfFs;
	}
	
	private void insertSave(T ins, String strV) throws IllegalArgumentException, IllegalAccessException, SQLException {
		StringBuilder sql = new StringBuilder("insert into `" + table + "` (");
		for(ColumnField cf: cfs) {
			if(strV == null && cf.column().name().equals(key.column().name())) {
				continue;
			}
			sql.append('`').append(cf.column().rawName()).append("`,");
		}
		if(cfs.length > 0) {
			sql.deleteCharAt(sql.length()-1);
		}
		sql.append(") values (");
		for(ColumnField cf: cfs) {
			if(strV == null && cf.column().name().equals(key.column().name())) {
				continue;
			}
			Object keyV = cf.field().get(ins);
			if(keyV == null) {
				sql.append("null").append(',');
			} else {
				sql.append(FieldReflect.formatObject(keyV)).append(',');
			}
		}
		if(cfs.length > 0) {
			sql.deleteCharAt(sql.length()-1);
		}
		sql.append(")");
		exe.execute(sql.toString());
	}
	

	private void updateSave(T ins, String strV) throws IllegalArgumentException, IllegalAccessException, SQLException {
		StringBuilder sql = new StringBuilder("update `" + table + "` set ");
		for(ColumnField cf: cfs) {
			if(strV == null && cf.column().name().equals(key.column().name())) {
				continue;
			}
			sql.append('`').append(cf.column().rawName()).append("`=");
			Object keyV = cf.field().get(ins);
			if(keyV == null) {
				sql.append("null").append(',');
			} else {
				sql.append(FieldReflect.formatObject(keyV)).append(',');
			}
		}
		if(cfs.length > 0) {
			sql.deleteCharAt(sql.length()-1);
		}
		sql.append(" where `" + key.column().rawName() + "`="+strV);
		exe.update(sql.toString());
	}
}
