package com.archer.framework.datasource.mysql.pool;

import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.archer.framework.datasource.exceptions.SqlException;
import com.archer.framework.datasource.mysql.MySQLConfig;
import com.mysql.cj.conf.ConnectionUrl;
import com.mysql.cj.jdbc.ConnectionImpl;
import com.mysql.cj.jdbc.JdbcConnection;

public class ArcherMysqlPool {
	
	private static final int poolSize = 8;
	
	private ArcherConnection[] conns;
	
	private SheduleThread schedule;
	
	private ArcherPoolExecutor executor;
	
	private Properties properties;
	
	private String jdbcUrl;
	
	public ArcherMysqlPool(MySQLConfig config) throws SQLException {
		conns = new ArcherConnection[poolSize];
		executor = new ArcherPoolExecutor();
		properties = new Properties();
		properties.setProperty("user", config.getUser());
		properties.setProperty("password", config.getPwd());
		jdbcUrl = config.getUrl();
		
		
		for(int i = 0; i < poolSize; i++) {
			conns[i] = new ArcherConnection(newConnection());
		}
		
		schedule = new SheduleThread(Void -> {
			for(ArcherConnection conn: conns) {
				try {
					conn.getConnection().pingInternal(true, 1000);
				} catch (SQLException ignore) {
					continue ;
				}
			}
			for(ArcherConnection conn: conns) {
				if(conn.isClosed()) {
					try {
						ArcherConnection newConn = new ArcherConnection(newConnection());
						conn = newConn;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		schedule.start();
	}
	
	public ArcherConnection getConnection() {
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
	
	protected JdbcConnection newConnection() throws SQLException {
		ConnectionUrl url = ConnectionUrl.getConnectionUrlInstance(jdbcUrl, properties);
		JdbcConnection conn = ConnectionImpl.getInstance(url.getMainHost());
		conn.setNetworkTimeout(executor, 1000);
		return conn;
	}
	
	class ArcherPoolExecutor implements Executor {

		@Override
		public void execute(Runnable command) {
			command.run();
		}
		
	}
}
