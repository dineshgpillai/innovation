package com.trade.imdg.hazelcast;

import static com.example.mu.database.MuSchemaConstants.HBASE_HOST;
import static com.example.mu.database.MuSchemaConstants.ZK_HOST;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.mu.database.Schema;
import com.example.mu.domain.Instrument;
import com.example.mu.domain.Party;
import com.example.mu.domain.Trade;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;

public class MainNoSpringBoot {
	public static final String MAP_INSTRUMENTS = "instrument";
	public static final Logger LOG = LoggerFactory.getLogger(MainNoSpringBoot.class);
	public static HazelcastInstance hz = Hazelcast.newHazelcastInstance();
	
	
	public static void startUpHz()throws Exception{
		 LOG.info("Done creating the schema tables");
			IMap<String, Instrument> instruments = hz
					.getMap(MAP_INSTRUMENTS);
			LOG.info("Size of Instruments is " + instruments.size());
			LOG.info("Loading instruments ...");
			
			//instruments.loadAll(true);
			
			if(instruments.size() > 3000) {
				
				LOG.info("Not loading from file " + instruments.size());
				return;
			}
		
	}

	public static void main(String[] args)  throws Exception{
		
		System.out.println("Starting up...");
		
		MainNoSpringBoot.startUpHz();
		//Create the Hbase schema tables
		/*Configuration config =  HBaseConfiguration.create();
        config.setInt("timeout", 120000);
        config.set("hbase.master", HBASE_HOST + ":60000");
        config.set("hbase.zookeeper.quorum",ZK_HOST);
        config.set("hbase.zookeeper.property.clientPort", "2181");
        //config.set("hbase.zookeeper.property.dataDir", "/mnt/data/zookeeper");
        //Schema.createSchemaTables(config);
*/		
		
		//IMap<String, Instrument> map = hz.getMap(Main.MAP_INSTRUMENTS);
		
		 
		
		
		
		System.out.println("Shutting down.");
	     
	   

	}

}
