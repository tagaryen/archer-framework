package com.archer.test.run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.archer.framework.base.annotation.Async;
import com.archer.framework.base.annotation.Inject;
import com.archer.framework.base.annotation.Log;
import com.archer.framework.base.annotation.Service;
import com.archer.framework.datasource.mysql.MySQLExecutor;
import com.archer.framework.datasource.mysql.Where;
import com.archer.log.Logger;

@Service
public class TestService implements TestServiceInter {
	
	@Log
	Logger log;
	
	@Inject
	SqlRepository sqlRepo;

	public ResponseVO test(String id, String pathVar, RequestVO vo) {
		log.info("get in service test1, id = {}, vo = {}", id, (vo == null?"null":vo));
		
//		test3(pathVar);
		
		ResponseVO res = new ResponseVO();
		long t0 = System.currentTimeMillis();
		SqlEntity entity = sqlRepo.findOneBy(new Where("column_a", Where.Types.EQ, "46852419614").and(new Where("column_b", Where.Types.EQ, 68)));

		long t1 = System.currentTimeMillis();
		List<SqlEntity> entities = sqlRepo.findListBy(new Where("column_c", Where.Types.BIGGER, 5000).or(new Where("column_b", Where.Types.SMALLER, 70).and(new Where("column_b", Where.Types.BIGGER, 60))));
		
		for(SqlEntity en: entities) {
			log.info("id = {}, column_a = {}", en.getId(), en.getColumnA());
		}

		long t2 = System.currentTimeMillis();
		entity.setColumnI(LocalDateTime.now());
		sqlRepo.save(entity);

		long t3 = System.currentTimeMillis();
		entity.setId(null);
		entity.setColumnE(pathVar);
		entity.setColumnA(entity.getColumnA() + 39);
		sqlRepo.save(entity);

		long t4 = System.currentTimeMillis();
		System.out.println("findOneBy: " + (t1 - t0) + ", findListBy: " + (t2 -t1) + ", update: " + (t3 - t2) + ", insert: " + (t4  -t3));
		res.id = entity.getColumnI().toString();
		res.pathVar = entity.getColumnE();
		res.req = vo.req;
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
