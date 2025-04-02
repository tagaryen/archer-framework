package com.archer.framework.datasource.mysql;

import java.sql.SQLException;

import com.archer.framework.base.annotation.Config;
import com.archer.framework.base.annotation.ConfigComponent;
import com.archer.framework.base.annotation.Log;
import com.archer.framework.base.annotation.Value;
import com.archer.log.Logger;

@Config
public class MySQLConfig {

	@Value(id = "archer.datasource.mysql.enabled", defaultVal = "true")
	private boolean enabled;

	@Value(id = "archer.datasource.mysql.showSql", defaultVal = "false")
	private boolean showSql;
	
	@Value(id = "archer.datasource.mysql.url")
	private String url;
	
	@Value(id = "archer.datasource.mysql.user")
	private String user;
	
	@Value(id = "archer.datasource.mysql.pwd")
	private String pwd;

	@Value(id = "archer.datasource.mysql.maxPoolSize", defaultVal = "60")
	private String maxPoolSize = "60";

	@Value(id = "archer.datasource.mysql.minIdle", defaultVal = "20")
	private String minIdle = "20";
	
	@Value(id = "archer.datasource.mysql.maxLifetime", defaultVal = "6000")
	private String maxLifetime = "6000";
	
	@Log
	Logger log;

	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isShowSql() {
		return showSql;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPwd() {
		return pwd;
	}
	
	public String getMaxPoolSize() {
		return maxPoolSize;
	}

	public String getMinIdle() {
		return minIdle;
	}

	public String getMaxLifetime() {
		return maxLifetime;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public void setMaxPoolSize(String maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public void setMinIdle(String minIdle) {
		this.minIdle = minIdle;
	}

	public void setMaxLifetime(String maxLifetime) {
		this.maxLifetime = maxLifetime;
	}

	@ConfigComponent(enabled = "archer.datasource.mysql.enabled")
	public MySQLExecutor initMySQLExecutor() throws SQLException {
		log.info("connect to mysql {}", url);
		return new MySQLExecutor(this, log);
	}
}
