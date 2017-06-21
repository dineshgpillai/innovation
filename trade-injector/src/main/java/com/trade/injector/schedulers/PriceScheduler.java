package com.trade.injector.schedulers;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.mu.domain.Instrument;
import com.example.mu.domain.Price;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.trade.injector.business.service.BusinessServiceCacheNames;
import com.trade.injector.business.service.GeneratePriceData;
import com.trade.injector.sinks.KafkaSink;

@Component
public class PriceScheduler {
	
	final Logger LOG = LoggerFactory.getLogger(PriceScheduler.class);
	
	@Autowired
	private KafkaSink sender;
	
	@Autowired
	HazelcastInstance hazelcastInstance;
	
	@Value("${kafka.topic.marketData}")
	private String marketDataTopic;
	
	
	
	/**
	 * This scheduler will generate prices every 1 minute
	 * for instruments that currently exist within the cache
	 * @throws Exception
	 */
	
	@Scheduled(fixedDelay=60000)
	public void generateFrequentPriceData() throws Exception{
		
		LOG.info("Starting to generate price...");
		//first get all the prices from the cache
		IMap<String, Instrument> mapInstruments = hazelcastInstance
				.getMap("instrument");
		
		//no data do not generate
		if(mapInstruments.size() == 0){
			LOG.warn("No instruments found to generate prices");
			return;
		}
		
		//generate and sink to Kafka
		Collection<Instrument> ins = mapInstruments.values();
		LOG.info("Number of Instruments "+ins.size());
		ins.stream().forEach(a->sender.send(marketDataTopic,GeneratePriceData.generateRandomDataOnInstruments(a).toJSON()));
		
		LOG.info("Price generation done");
	}
	
	@Scheduled(fixedDelay=60000)
	public void getUpdatedPriceData() throws Exception{
		
		LOG.info("Starting to obtain prices...");
		//first get all the prices from the cache
		IMap<String, Price> mapPrices = hazelcastInstance
				.getMap(BusinessServiceCacheNames.PRICE_CACHE);
		
		//no data do not generate
		if(mapPrices.size() == 0){
			LOG.warn("No prices found");
			return;
		}
		
		//generate and sink to Kafka
		Collection<Price> px = mapPrices.values();
		LOG.info("Number of prices "+px.size());
		//ins.stream().forEach(a->sender.send(marketDataTopic,GeneratePriceData.generateRandomDataOnInstruments(a).toJSON()));
		
		LOG.info("Prices obtained done");
	}

}
