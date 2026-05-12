package com.archer.framework.datasource.sqlite;

import java.sql.SQLException;

import com.archer.framework.base.annotation.Config;
import com.archer.framework.base.annotation.ConfigComponent;
import com.archer.framework.base.annotation.Log;
import com.archer.framework.base.annotation.Value;
import com.archer.log.Logger;

@Config
public class SqliteConfig {

	@Value(id = "archer.datasource.sqlite.enabled", defaultVal = "false")
	private boolean enabled;

	@Value(id = "archer.datasource.sqlite.showSql", defaultVal = "false")
	private boolean showSql;
	
	@Value(id = "archer.datasource.sqlite.url", defaultVal = "jdbc:sqlite:sqlite/archer.db")
	private String url;

	@Value(id = "archer.datasource.sqlite.explicitReadOnly", defaultVal = "false")
	private boolean explicitReadOnly = false;

	@Value(id = "archer.datasource.sqlite.transactionMode", defaultVal = "DEFERRED")
	private String transactionMode = "DEFERRED";
	
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
	
	public boolean isExplicitReadOnly() {
		return explicitReadOnly;
	}

	public String getTransactionMode() {
		return transactionMode;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setExplicitReadOnly(boolean explicitReadOnly) {
		this.explicitReadOnly = explicitReadOnly;
	}

	public void setTransactionMode(String transactionMode) {
		this.transactionMode = transactionMode;
	}

	@ConfigComponent(enabled = "archer.datasource.sqlite.enabled")
	public SqliteExecutor initExecutor() throws SQLException {
		log.info("connect to sqlite {}", url);
		return new SqliteExecutor(this, log);
	}
}
