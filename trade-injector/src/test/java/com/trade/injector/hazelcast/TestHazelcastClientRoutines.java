package com.trade.injector.hazelcast;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.mu.domain.Instrument;
import com.example.mu.domain.Party;
import com.example.mu.domain.Trade;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import com.trade.injector.business.service.GenerateInstrumentCache;
import com.trade.injector.business.service.GeneratePartyCache;
import com.trade.injector.business.service.GenerateTradeCacheData;
import com.trade.injector.controller.TradeInjectorController;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeInjectorController.class)
public class TestHazelcastClientRoutines {

	@Autowired
	HazelcastInstance hzInstance;

	

	@Before
	public void setUpCache() {

		
	}

	/*
	 * @Test public void testHZstart() { Map<String, Trade> map =
	 * hzInstance.getMap("trade"); assertNotNull(map);
	 * 
	 * map = hzInstance.getMap("trade");
	 * 
	 * assertEquals(1, map.size());
	 * 
	 * }
	 */
	@Test
	public void testClientGeneration() {
		IMap<String, Party> map = hzInstance.getMap("party");
		assertNotNull(map);

		assertEquals(0, map.size());

		Predicate predicate = new SqlPredicate(String.format("name like %s",
				"TRDINJECT_CLI_%"));
		Set<Party> parties = (Set<Party>) map.values(predicate);

		assertEquals(0, parties.size());

		// now add parties and make sure there is one
		GeneratePartyCache cacheGenerator = new GeneratePartyCache();
		cacheGenerator.populateMap(1, map);

		map = hzInstance.getMap("party");
		assertEquals(1, map.size());

		Set<Party> parties2 = (Set<Party>) map.values(predicate);
		assertEquals(1, parties2.size());

		// we need the key set to remove
		Set<String> partiesKeys = (Set<String>) map.keySet(predicate);
		assertEquals(1, partiesKeys.size());

		// now delete the parties
		for (String aparty : partiesKeys) {


			System.out.println("Party in question is " + aparty);
			map.remove(aparty);
		}

		// now check hz there should be none in there
		map = hzInstance.getMap("party");
		Set<Party> parties3 = (Set<Party>) map.values(predicate);
		assertEquals(0, parties3.size());

		map = hzInstance.getMap("party");
		assertEquals(0, map.size());
	}
	
	@Test
	public void testTradeGeneration() throws Exception{
		
		//first generate party information
		IMap<String, Party> map = hzInstance.getMap("party");
		assertNotNull(map);

		assertEquals(0, map.size());

		GeneratePartyCache cacheGenerator = new GeneratePartyCache();
		cacheGenerator.populateMap(1000, map);

		map = hzInstance.getMap("party");
		assertEquals(1000, map.size());

		
		//now generate instrument information
		System.out.println("Runnning many instruments...");

		IMap<String, Instrument> mapInstruments = hzInstance.getMap("instrument");
		assertNotNull(mapInstruments);

		assertEquals(0, mapInstruments.size());

		GenerateInstrumentCache cacheGeneratorInstruments = new GenerateInstrumentCache();
		cacheGeneratorInstruments.populateMap(1000, mapInstruments);

		mapInstruments = hzInstance.getMap("instrument");
		assertEquals(1000, mapInstruments.size());
		
		//now generate trades
		IMap<String, Trade> mapTrades = hzInstance.getMap("trade");
		assertEquals(0, mapTrades.size());
		GenerateTradeCacheData tradeGen = new GenerateTradeCacheData();
		for(int i=0; i<100; i++){
			//generate 100 trades
			Trade[] trades = tradeGen.createTrade(i, map, mapInstruments);
			mapTrades.put(trades[0].getExecutionId(), trades[0]); //for buy 
			mapTrades.put(trades[1].getExecutionId(), trades[1]); //for sell
		}

		//now check to see if it is done
		mapTrades = hzInstance.getMap("trade");
		assertEquals(200, mapTrades.size());
		
		
	}
	
	@Test
	public void testConversionToReport() throws Exception{
		System.out.println("test report generation...");
		testTradeGeneration();
		IMap mapTrades = hzInstance.getMap("trade");
		assertEquals(200, mapTrades.size());
		
		
	}

	@Test
	public void testManyClients() {

		System.out.println("Runnning many clients...");

		IMap<String, Party> map = hzInstance.getMap("party");
		assertNotNull(map);

		assertEquals(0, map.size());

		GeneratePartyCache cacheGenerator = new GeneratePartyCache();
		cacheGenerator.populateMap(1000, map);

		map = hzInstance.getMap("party");
		assertEquals(1000, map.size());

		
		System.out.println("Finished");

	}

	@Test
	public void testManyClientsAndIncrease() {

		System.out.println("Runnning many clients and more...");

		IMap<String, Party> map = hzInstance.getMap("party");
		assertNotNull(map);

		assertEquals(0, map.size());

		GeneratePartyCache cacheGenerator = new GeneratePartyCache();
		cacheGenerator.populateMap(1000, map);

		map = hzInstance.getMap("party");
		assertEquals(1000, map.size());

		// now increase to 1500
		cacheGenerator.populateMap(1500, map);
		assertEquals(1500, map.size());

		
		System.out.println("Finished");

	}
	
	@Test
	public void generateInstruments(){
		
		//instrument
		System.out.println("Runnning many instruments...");

		IMap<String, Instrument> map = hzInstance.getMap("instrument");
		assertNotNull(map);

		assertEquals(0, map.size());

		GenerateInstrumentCache cacheGenerator = new GenerateInstrumentCache();
		cacheGenerator.populateMap(1000, map);

		map = hzInstance.getMap("instrument");
		assertEquals(1000, map.size());

		
		
		
		
	}

	@After
	public void removeCache() {

		IMap<String, Trade> mapTrade = hzInstance.getMap("trade");
		assertNotNull(mapTrade);

		
		
		Predicate predicateTrade = new SqlPredicate(String.format("executionVenueId = %s",
				"EX1"));
		
		Set<String> tradeId = (Set<String>)mapTrade.keySet(predicateTrade);
		for(String key : tradeId){
			mapTrade.remove(key);
		}
		
		mapTrade = hzInstance.getMap("trade");
		assertEquals(0, mapTrade.size());
		
		
		//now delete Instruments
		IMap<String, Instrument> mapInstruments = hzInstance.getMap("instrument");
		assertNotNull(mapInstruments);
		
		Predicate predicate = new SqlPredicate(String.format("symbol like %s",
				"ISIN_INJ_%"));
		Set<String> ins = (Set<String>) mapInstruments.keySet(predicate);

		for (String aIns : ins) {
			mapInstruments.remove(aIns);
		}

		mapInstruments = hzInstance.getMap("instrument");
		assertEquals(0, mapInstruments.size());

		
		//now delete parties
		IMap<String, Instrument> parties = hzInstance.getMap("party");
		assertNotNull(parties);
		
		Predicate predicateParty = new SqlPredicate(String.format("name like %s",
				"TRDINJECT_CLI_%"));
		Set<String> partiesKey = (Set<String>) parties.keySet(predicateParty);

		for (String aParty : partiesKey) {
			parties.remove(aParty);
		}

		parties = hzInstance.getMap("party");
		assertEquals(0, parties.size());

		System.out.println("Finished");

		
		
		
		
		

	}

}
