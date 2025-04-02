package com.archer.framework.datasource.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.archer.log.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLExecutor {
    
    private MySQLConfig config;
    private HikariDataSource dataSource;
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
    	Properties properties = new Properties();
    	properties.setProperty("username", config.getUser());
    	properties.setProperty("password", config.getPwd());
    	properties.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
    	properties.setProperty("jdbcUrl", url);
    	properties.setProperty("maximumPoolSize", config.getMaxPoolSize());
    	properties.setProperty("minimumIdle", config.getMinIdle());
    	properties.setProperty("maxLifetime", config.getMaxLifetime());
    	this.dataSource = new HikariDataSource(new HikariConfig(properties));
    	this.log = log;
    }
    
    public <T> List<T> query(String sql, Class<T> cls) throws SQLException {
    	if(config.isShowSql()) {
        	log.info("sql: ", sql);
    	}
    	PreparedStatement statement = this.dataSource.getConnection().prepareStatement(sql);
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
    }
    
    public ResultSet executeQuery(String sql) throws SQLException {
    	if(config.isShowSql()) {
        	log.info("sql: ", sql);
    	}
    	PreparedStatement statement = this.dataSource.getConnection().prepareStatement(sql);
    	ResultSet result = statement.executeQuery();
    	statement.close();
		return result;
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
    	PreparedStatement statement = this.dataSource.getConnection().prepareStatement(sql);
    	statement.execute();
    	statement.close();
    }
    
    public int update(String sql) throws SQLException {
    	if(config.isShowSql()) {
        	log.info("sql: ", sql);
    	}
    	PreparedStatement statement = this.dataSource.getConnection().prepareStatement(sql);
    	int count = statement.executeUpdate();
    	statement.close();
		return count;
    }
    
    public List<Column> showColumns(String tableName) throws SQLException {
		Statement statement = this.dataSource.getConnection().createStatement();
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
    }

	public MySQLConfig getConfig() {
		return config;
	}
}
