package com.archer.framework.datasource.mysql;

import java.sql.SQLException;

import com.archer.framework.base.annotation.Config;
import com.archer.framework.base.annotation.ConfigComponent;
import com.archer.framework.base.annotation.Log;
import com.archer.framework.base.annotation.Value;
import com.archer.log.Logger;

@Config
public class MySQLConfig {

	@Value(id = "archer.datasource.mysql.enabled")
	private boolean enabled;
	
	@Value(id = "archer.datasource.mysql.url")
	private String url;
	
	@Value(id = "archer.datasource.mysql.user")
	private String user;
	
	@Value(id = "archer.datasource.mysql.pwd")
	private String pwd;
	
	@Log
	Logger log;

	public boolean isEnabled() {
		return enabled;
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

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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
	
	@ConfigComponent
	public MySQLExecutor initMySQLExecutor() throws SQLException {
		if(enabled) {
			log.info("connect to mysql {}", url);
			return new MySQLExecutor(this);
		}
		return null;
	}
}
