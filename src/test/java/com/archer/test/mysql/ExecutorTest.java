package com.archer.test.mysql;

import java.util.List;

import com.archer.framework.datasource.mysql.MySQLConfig;
import com.archer.framework.datasource.mysql.MySQLExecutor;
import com.archer.test.run.SqlEntity;

public class ExecutorTest {
	
	public static void main(String[] args) {
    	String url = "jdbc:mysql://10.32.122.173:9030/test_doris?characterEncoding=UTF-8&serverTimezong=Asia/Shanghai";
    	try {
    		MySQLConfig config = new MySQLConfig();
    		config.setUrl(url);
    		config.setUser("root");
    		config.setPwd("root_pwd");
    		MySQLExecutor exe = new MySQLExecutor(config, null);
    		exe.execute("insert into test_tb values (2, 2, 3.0, 4.1, '帅哥', '大帅哥', '2024-09-07', '2024-09-01 23:09:21', '2024-09-01 23:09:20')");
    		List<SqlEntity> ens = exe.query("select * from test_tb", SqlEntity.class);
    		System.out.println(ens.size());
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}
