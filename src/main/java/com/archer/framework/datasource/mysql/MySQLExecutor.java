package com.archer.framework.datasource.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.archer.framework.datasource.mysql.pool.ArcherConnection;
import com.archer.framework.datasource.mysql.pool.ArcherMysqlPool;
import com.archer.log.Logger;

public class MySQLExecutor {
    
    private MySQLConfig config;
    private ArcherMysqlPool dataSource;
    private ColumnReflect cref;
    private Logger log;
    
    public MySQLExecutor(MySQLConfig config, Logger log) throws SQLException {
    	this.cref = new ColumnReflect();
    	this.config = config;
    	String url = config.getUrl();
    	if(!url.contains("useServerPreparedStmts")) {
    		url += "&useServerPreparedStmts=true";
    	}
    	if(!url.contains("cachePrepStmts")) {
    		url += "&cachePrepStmts=true";
    	}
    	if(!url.contains("sendStringParametersAsUnicode")) {
    		url += "&sendStringParametersAsUnicode=false";
    	}
    	if(!url.contains("useAffectedRows")) {
    		url += "&useAffectedRows=false";
    	}
    	if(!url.contains("autoReconnect")) {
    		url += "&autoReconnect=true";
    	}
    	this.dataSource = new ArcherMysqlPool(config);
    	this.log = log;
    }
    
    public <T> List<T> query(String sql, Class<T> cls) throws SQLException {
    	if(config.isShowSql()) {
        	log.info("sql: ", sql);
    	}
    	ArcherConnection archerConn = this.dataSource.getConnection();
    	try {
        	PreparedStatement statement = archerConn.getConnection().prepareStatement(sql);
        	ResultSet result = statement.executeQuery();
    		ResultSetMetaData meta = result.getMetaData();
    		Column[] columns = new Column[meta.getColumnCount()+1];
    		for(int i = 1; i <= meta.getColumnCount(); i++) {
    			columns[i] = new Column(meta.getColumnName(i));
    		}
    		List<T> results = new ArrayList<>(128);
    		while(result.next()) {
    			results.add(cref.newInstanceAndSetColumns(columns, result, cls));
    		}
        	statement.close();
        	return results;
    	} finally {
    		archerConn.setUnUsed();
    	}
    }
    
    public ResultSet executeQuery(String sql) throws SQLException {
    	if(config.isShowSql()) {
        	log.info("sql: ", sql);
    	}
    	ArcherConnection archerConn = this.dataSource.getConnection();
    	try {
        	PreparedStatement statement = archerConn.getConnection().prepareStatement(sql);
        	ResultSet result = statement.executeQuery();
        	statement.close();
    		return result;
    	} finally {
    		archerConn.setUnUsed();
    	}
    }
    
    public <T> T queryOne(String sql, Class<T> cls) throws SQLException {
		List<T> results = query(sql, cls);
		if(results.size() > 1) {
			throw new SQLException("found " + results.size() + " results while only one is required");
		}
		if(results.size() == 0) {
			return null;
		}
    	return results.get(0);
    }
    
    public void execute(String sql) throws SQLException {
    	if(config.isShowSql()) {
        	log.info("sql: ", sql);
    	}
    	ArcherConnection archerConn = this.dataSource.getConnection();
    	try {
        	PreparedStatement statement = archerConn.getConnection().prepareStatement(sql);
        	statement.execute();
        	statement.close();
    	} finally {
    		archerConn.setUnUsed();
    	}
    }
    
    public int update(String sql) throws SQLException {
    	if(config.isShowSql()) {
        	log.info("sql: ", sql);
    	}
    	ArcherConnection archerConn = this.dataSource.getConnection();
    	try {
        	PreparedStatement statement = archerConn.getConnection().prepareStatement(sql);
        	int count = statement.executeUpdate();
        	statement.close();
    		return count;
    	} finally {
    		archerConn.setUnUsed();
    	}
    }
    
    public List<Column> showColumns(String tableName) throws SQLException {
    	ArcherConnection archerConn = this.dataSource.getConnection();
    	try {
    		Statement statement = archerConn.getConnection().createStatement();
    		ResultSet result = statement.executeQuery("describe `" + tableName + "`");
    		ResultSetMetaData meta = result.getMetaData();
    		int fieldIndex = 0, typeIndex = 0, keyIndex = 0;
    		for(int i = 1; i <= meta.getColumnCount(); i++) {
    			if("Field".equals(meta.getColumnName(i))) {
    				fieldIndex = i;
    			} else if("Type".equals(meta.getColumnName(i))) {
    				typeIndex = i;
    			} else if("Key".equals(meta.getColumnName(i))) {
    				keyIndex = i;
    			}
    		}
    		if(fieldIndex == 0 || typeIndex == 0 || keyIndex == 0) {
    			throw new SQLException("Invalid table " + tableName);
    		}
    		List<Column> columns = new ArrayList<>(48);
    		while(result.next()) {
    			String name = result.getString(fieldIndex);
    			String type = result.getString(typeIndex);
    			String key = result.getString(keyIndex);
    			columns.add(new Column(name, type, "PRI".equals(key)));
    		}
    		return columns;
    	} finally {
    		archerConn.setUnUsed();
    	}
    }

	public MySQLConfig getConfig() {
		return config;
	}
}
