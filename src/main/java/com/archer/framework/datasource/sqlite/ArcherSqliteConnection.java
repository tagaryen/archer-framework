package com.archer.framework.datasource.sqlite;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sqlite.SQLiteConnection;


public class ArcherSqliteConnection {

	private SQLiteConnection conn;
	private AtomicBoolean used;
	
	public ArcherSqliteConnection(SQLiteConnection conn) throws SQLException {
		this.conn = conn;
		this.used = new AtomicBoolean(false);
	}
	
	public boolean isClosed() {
		try {
			return this.conn.isClosed();
		} catch (SQLException e) {
			return true;
		}
	}
	
	public boolean isUsable() {
		return this.used.compareAndSet(false, true);
	}
	
	public SQLiteConnection getConnection() {
		return conn;
	}
	
	public void setUnUsed() {
		this.used.set(false);
	}
	
}
