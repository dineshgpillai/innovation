package com.trade.imdg.hazelcast;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.mu.domain.Instrument;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;


@SpringBootApplication (scanBasePackages ="com.trade.imdg")
public class Main {
	
	public static final String MAP_INSTRUMENTS= "instrument";
	public final Logger LOG = LoggerFactory.getLogger(Main.class);
	
	
	public static void main(String[] args) {

		SpringApplication.run(Main.class, args);
		

		HazelcastInstance hz = Hazelcast.newHazelcastInstance();

		// JetInstance instance1 = Jet.newJetInstance();
		// JetInstance instance2 = Jet.newJetInstance();

		// Will clean up when there's no exception:
		// Jet.shutdownAll();

		try {
			
			JetConfig config = new JetConfig();
			config.getInstanceConfig().setCooperativeThreadCount(2);


			JetInstance instance = Jet.newJetInstance(config);
			Jet.newJetInstance(config);
			
			IMap<String, Instrument> instruments= hz.getMap(MAP_INSTRUMENTS);
			System.out.println("Size of Instruments is "+ instruments.size());
			System.out.println("Loading instruments ...");
		
			
			for(int i=0; i<10; i++){
				
				int numberIdentifier = i++;
				String key = UUID.randomUUID().toString();
				Instrument aIns = new Instrument();
				aIns.setAssetClass("COMMODITY");
				aIns.setInstrumentId(key);
				aIns.setIssuer("Exchange");
				aIns.setProduct("PROD_INJ_"+numberIdentifier);
				aIns.setSymbol("ISIN_INJ_"+numberIdentifier);
				
				instruments.put(key, aIns);
				
			}
			
			System.out.println("Size of Instruments is "+ instruments.size());
			//instruments.loadAll(false);

		} finally {
			//Jet.shutdownAll();
		}

	}
}
