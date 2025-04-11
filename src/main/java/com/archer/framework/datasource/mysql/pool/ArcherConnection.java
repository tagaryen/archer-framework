package com.archer.framework.datasource.mysql.pool;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mysql.cj.jdbc.JdbcConnection;

public class ArcherConnection {

	private JdbcConnection conn;
	private AtomicBoolean used;
	
	public ArcherConnection(JdbcConnection conn) throws SQLException {
		conn.pingInternal(true, 1000);
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
	
	public JdbcConnection getConnection() {
		return conn;
	}
	
	public void setUnUsed() {
		this.used.set(false);
	}
	
}
