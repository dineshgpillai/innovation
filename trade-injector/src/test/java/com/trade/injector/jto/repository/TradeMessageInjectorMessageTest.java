package com.trade.injector.jto.repository;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.util.DBObjectUtils;
import org.springframework.test.context.junit4.SpringRunner;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.trade.injector.application.Application;
import com.trade.injector.jto.TradeInjectorMessage;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TradeMessageInjectorMessageTest {
	
	@Autowired
	private MongoDBTemplate template;
	
	@Before
	public void testData(){
		
		
		System.out.println("Preparing Test data");
		TradeInjectorMessage message = new TradeInjectorMessage();
		DBObject dbObject = createDBObject(message);
		DB db = template.getDbFactory().getDb();
		DBCollection coll = db.createCollection("tradeInjector", dbObject);
		
		
	}
	
	private static DBObject createDBObject(TradeInjectorMessage message) {
		BasicDBObjectBuilder docBuilder = BasicDBObjectBuilder.start();
								
		docBuilder.append("_id", message.getUserId());
		docBuilder.append("noOfClients", message.getNoOfClients());
		docBuilder.append("noOfTrades", message.getNoOfTrades());
		docBuilder.append("noOfInstruments", message.getNoOfInstruments());
		return docBuilder.get();
	}

	@Test
	public void testIfCollectionExists(){
		DB db = template.getDbFactory().getDb();
		DBCollection coll = db.getCollection("tradeInjector");
		
		assertNotNull(coll);
		
	}
	
	@Test
	public void testInsertion(){
		DB db = template.getDbFactory().getDb();
		DBCollection coll = db.getCollection("tradeInjector");
		
		//create one new document
		TradeInjectorMessage message = new TradeInjectorMessage();
		message.setNoOfClients("10");
		message.setNoOfInstruments("10");
		message.setNoOfTrades("1000");
		message.setTradeDate(new Date(System.currentTimeMillis()).toString());
		message.setTimeDelay("1000");
		BasicDBObject documentDetail = new BasicDBObject();
		documentDetail.put("id", message);
		documentDetail.put("noOfClients", message.getNoOfClients());
		documentDetail.put("noOfInstruments", message.getNoOfInstruments());
		documentDetail.put("noOfTrades", message.getNoOfTrades());
		documentDetail.put("tradeDate", message.getTradeDate());
		documentDetail.put("timeDelay", message.getTimeDelay());
		
		coll.insert(documentDetail);
		
		DBCursor cursorDoc = coll.find();
		assertEquals(1, cursorDoc.count());
		
	}
	
	@Test
	public void testDelete(){
		
	}
	
	@Test
	public void testReadAll(){
		
	}
	
	@Test
	public void testReadOne(){
		
	}
	
	@Test
	public void testUpdateOne(){
		
	}
	
	
	
	@After
	public void deleteAll(){
		System.out.println("Destroying data");
		
		DB db = template.getDbFactory().getDb();
		DBCollection coll = db.getCollection("tradeInjector");
		//drop the entire collection
		
		
		if(coll != null)
			coll.drop();
		
		
	}
	
	
	

}
