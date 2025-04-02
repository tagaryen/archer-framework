package com.archer.test.mysql;

import com.archer.framework.datasource.mysql.MySQLConfig;
import com.archer.framework.datasource.mysql.MySQLExecutor;
import com.archer.test.run.SqlEntity;

public class ExecutorTest {
	
	public static void main(String[] args) {
    	String url = "jdbc:mysql://10.32.122.173:4406/node2?characterEncoding=UTF-8&serverTimezong=Asia/Shanghai";
    	try {
    		MySQLConfig config = new MySQLConfig();
    		config.setUrl(url);
    		config.setUser("secret");
    		config.setPwd("secret_test");
    		MySQLExecutor exe = new MySQLExecutor(config, null);
    		exe.query("select * from sqltest", SqlEntity.class);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}
