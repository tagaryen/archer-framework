package com.archer.test.run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import com.archer.framework.base.annotation.Async;
import com.archer.framework.base.annotation.Inject;
import com.archer.framework.base.annotation.Log;
import com.archer.framework.base.annotation.Service;
import com.archer.framework.datasource.mysql.MySQLExecutor;
import com.archer.log.Logger;

@Service
public class TestService implements TestServiceInter {
	
	@Log
	Logger log;
	
	@Inject
	MySQLExecutor mysql;

	public ResponseVO test(String id, String pathVar, RequestVO vo) {
		log.info("get in service test1, id = {}, vo = {}", id, (vo == null?"null":vo));
		
		test3(pathVar);
		
		ResponseVO res = new ResponseVO();
		try {
			SqlEntity entity = mysql.queryOne("select * from sqltest limit 1", SqlEntity.class);
			res.id = entity.getColumnI().toString();
			res.pathVar = entity.getColumnE();
			res.req = vo.req;
		} catch (SQLException e) {
			e.printStackTrace();
			res.id = id;
			res.pathVar = pathVar;
			res.req = vo.req;
		}
		return res;
	}
	
	public ResponseVO test2(String queryP, MultipartVO vo) {
		
		log.info("get in service test2, queryP = {}", queryP);

		log.info("get in service test2, req = {}", vo.req);
		
		try {
			Files.write(Paths.get("e:/tmp.xml"), vo.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ResponseVO res = new ResponseVO();
		res.id = "thisID";
		res.pathVar = queryP;
		res.req = vo.req;
		return res;
	}
	
	@Async
	public void test3(String queryP) {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("get in service test3, queryP = {}", queryP);
		
	}
	
	public class RequestVO {
		String req;
	}
	
	public class MultipartVO {
		String req;
		byte[] file;
	}
	
	public class ResponseVO {
		
		String id;
		
		String msg = "xuyi";

		String pathVar;
		
		String req;
		
	}
}
