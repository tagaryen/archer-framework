package com.archer.framework.datasource.mysql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mysql.cj.conf.ConnectionUrl;
import com.mysql.cj.jdbc.ha.LoadBalancedConnectionProxy;
import com.mysql.cj.jdbc.ha.LoadBalancedMySQLConnection;

public class MySQLExecutor {
    
    private MySQLConfig config;
    private LoadBalancedMySQLConnection conncetion;
    private ColumnReflect cref;
    
    public MySQLExecutor(MySQLConfig config) throws SQLException {
    	Properties properties = new Properties();
    	properties.setProperty("user", config.getUser());
    	properties.setProperty("password", config.getPwd());
    	this.cref = new ColumnReflect();
    	this.config = config;
    	this.conncetion = new LoadBalancedMySQLConnection(new LoadBalancedConnectionProxy(ConnectionUrl.getConnectionUrlInstance(config.getUrl(), properties)));
    }
    
    public <T> List<T> query(String sql, Class<T> cls) throws SQLException {
		Statement statement = this.conncetion.createStatement();
		ResultSet result = statement.executeQuery(sql);
		ResultSetMetaData meta = result.getMetaData();
		Column[] columns = new Column[meta.getColumnCount()+1];
		for(int i = 1; i <= meta.getColumnCount(); i++) {
			columns[i] = new Column(meta.getColumnName(i), meta.getColumnType(i));
		}
		List<T> results = new ArrayList<>(128);
		while(result.next()) {
			results.add(cref.newInstanceAndSetColumns(columns, result, cls));
		}
    	return results;
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
		Statement statement = this.conncetion.createStatement();
		statement.execute(sql);
    }
    
    public int update(String sql) throws SQLException {
		Statement statement = this.conncetion.createStatement();
		return statement.executeUpdate(sql);
    }

	public MySQLConfig getConfig() {
		return config;
	}
	
}
