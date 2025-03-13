package com.archer.test.mysql;

import java.util.List;

import com.archer.framework.datasource.mysql.MySQLConfig;
import com.archer.framework.datasource.mysql.MySQLExecutor;

public class ExecutorTest {
	public static void main(String[] args) {
    	String url = "jdbc:mysql://10.32.122.173:4406/node2?characterEncoding=UTF-8&serverTimezong=Asia/Shanghai";
    	try {
    		MySQLConfig config = new MySQLConfig();
    		config.setUrl(url);
    		config.setUser("secret");
    		config.setPwd("secret_test");
    		MySQLExecutor exe = new MySQLExecutor(config);
    		List<SqlEntity> entities = exe.query("select * from sqltest", SqlEntity.class);
    		for(SqlEntity en: entities) {
    			System.out.println(en.getColumnG() + ",   " + en.getColumnH() + ",  " + en.getColumnI());
    		}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}
