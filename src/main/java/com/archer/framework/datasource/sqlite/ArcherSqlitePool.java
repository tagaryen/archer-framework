package com.archer.framework.datasource.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		createDirectories();
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
	
	private void createDirectories() {
		String url = config.getUrl();
		if(!url.startsWith("jdbc:sqlite:")) {
			throw new SqlException("Invalid sqlite url '" + url + "'");
		}
		String filePath = url.substring(12);
		int slash = filePath.lastIndexOf('/');
		if(slash > 0) {
			Path dir = Paths.get(filePath.substring(0, slash));
			if(!Files.exists(dir)) {
				try {
					Files.createDirectories(dir);
				} catch (IOException e) {
					throw new SqlException("Could not create directories " + dir.toString());
				}
			}
		}
		
	}
}
