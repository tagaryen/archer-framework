package com.archer.framework.datasource.mysql;

import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.archer.framework.datasource.exceptions.SqlException;
import com.mysql.cj.conf.ConnectionUrl;
import com.mysql.cj.jdbc.ConnectionImpl;
import com.mysql.cj.jdbc.JdbcConnection;

public class ArcherMysqlPool {
	
	private static final int poolSize = 8;
	
	private ArcherMysqlConnection[] conns;
	
	private SheduleThread schedule;
	
	private ArcherPoolExecutor executor;
	
	private Properties properties;
	
	private String jdbcUrl;
	
	public ArcherMysqlPool(MySQLConfig config) throws SQLException {
		conns = new ArcherMysqlConnection[poolSize];
		executor = new ArcherPoolExecutor();
		properties = new Properties();
		properties.setProperty("user", config.getUser());
		properties.setProperty("password", config.getPwd());
		jdbcUrl = config.getUrl();
		
		
		for(int i = 0; i < poolSize; i++) {
			conns[i] = new ArcherMysqlConnection(newConnection());
		}
		
		schedule = new SheduleThread(Void -> {
			for(ArcherMysqlConnection conn: conns) {
				try {
					conn.getConnection().pingInternal(true, 1000);
				} catch (SQLException ignore) {
					continue ;
				}
			}
			for(ArcherMysqlConnection conn: conns) {
				if(conn.isClosed()) {
					try {
						ArcherMysqlConnection newConn = new ArcherMysqlConnection(newConnection());
						conn = newConn;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		schedule.start();
	}
	
	public ArcherMysqlConnection getConnection() {
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
	

	class SheduleThread extends Thread {
		
		private static final String namePrefix = "ArcherConnectPool-";
		private boolean running;
		private long scheduleTime = 2000;
		Consumer<Void> task;
		
		public SheduleThread(Consumer<Void> task) {
			super(namePrefix);
			this.task = task;
		}
	
		public void exit() {
			running = false;
			super.interrupt();
		}
		
		@Override
		public void start() {
			running = true;
			super.start();
		}
		
		
		
		@Override
		public void run() {
			long start, sleep = scheduleTime;
			while(running) {
				start = System.currentTimeMillis();
				try {
					Thread.sleep(sleep);
				} catch (Exception ignore) {
					sleep = sleep - (System.currentTimeMillis() - start); 
					continue;
				}
				sleep = scheduleTime;
				this.task.accept(null);
			}
		}
	}
}
