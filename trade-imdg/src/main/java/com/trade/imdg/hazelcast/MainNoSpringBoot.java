package com.trade.imdg.hazelcast;

import static com.example.mu.database.MuSchemaConstants.HBASE_HOST;
import static com.example.mu.database.MuSchemaConstants.ZK_HOST;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

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

	public static void main(String[] args)  throws Exception{
		
		System.out.println("Starting up...");
		
		
		//Create the Hbase schema tables
		/*Configuration config =  HBaseConfiguration.create();
        config.setInt("timeout", 120000);
        config.set("hbase.master", HBASE_HOST + ":60000");
        config.set("hbase.zookeeper.quorum",ZK_HOST);
        config.set("hbase.zookeeper.property.clientPort", "2181");
        //config.set("hbase.zookeeper.property.dataDir", "/mnt/data/zookeeper");
        //Schema.createSchemaTables(config);
*/		
		HazelcastInstance hz = Hazelcast.newHazelcastInstance();
		
		IMap<String, Instrument> map = hz.getMap(Main.MAP_INSTRUMENTS);
		
		 
		while(true){
			Thread.sleep(30000);
			System.out.println("Map size is "+map.size());
		}
		
		//load the map with instrument data
		
		
		//System.out.println("Shutting down.");
	     
	   

	}

}
