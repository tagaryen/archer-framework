package com.archer.framework.datasource.sqlite;

import java.sql.SQLException;
import java.util.Properties;

import org.sqlite.JDBC;
import org.sqlite.SQLiteConnection;
import com.archer.framework.datasource.exceptions.SqlException;

public class ArcherSqlitePool {
	
	private static final int poolSize = 8;
	
	private ArcherSqliteConnection[] conns;
	
	private SqliteConfig config;
	
	public ArcherSqlitePool(SqliteConfig appConf) throws SQLException {
		conns = new ArcherSqliteConnection[poolSize];
		config = appConf;
		
		
		for(int i = 0; i < poolSize; i++) {
			conns[i] = new ArcherSqliteConnection(newConnection());
		}
	}
	
	public ArcherSqliteConnection getConnection() {
		int i = 0;
		while(i < poolSize) {
			if(conns[i].isUsable()) {
				return conns[i];
			}
			i++;
			if(i == poolSize) {
				i = 0;
			}
		}
		throw new SqlException("can not get connection");
	}
	
	protected SQLiteConnection newConnection() throws SQLException {
        Properties properties = new Properties();
		if(config.isExplicitReadOnly()) {
			properties.setProperty("jdbc.explicit_readonly", "true");
		}
		if(!config.getTransactionMode().isEmpty()) { //DEFERRED,IMMEDIATE,EXCLUSIVE;
			properties.setProperty("transaction_mode", config.getTransactionMode());
		}
		
		SQLiteConnection conn = JDBC.createConnection(config.getUrl(), properties);
		return conn;
	}
}
