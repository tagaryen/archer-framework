package com.archer.framework.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.archer.framework.datasource.sqlang.Column;

public interface ArcherExecutor {
	<T> List<T> query(String sql, Class<T> cls) throws SQLException;
	ResultSet executeQuery(String sql) throws SQLException;
	<T> T queryOne(String sql, Class<T> cls) throws SQLException;
	void execute(String sql) throws SQLException;
	int update(String sql) throws SQLException;
	List<Column> showColumns(String tableName) throws SQLException;
}
