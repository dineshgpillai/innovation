package com.trade.imdg.hazelcast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;

@SpringBootApplication
public class Main {
	public static void main(String[] args) {
		
		SpringApplication.run(Main.class, args);

		HazelcastInstance hz = Hazelcast.newHazelcastInstance();
	    
		//JetInstance instance1 = Jet.newJetInstance();
        //JetInstance instance2 = Jet.newJetInstance();

        

        // Will clean up when there's no exception:
        //Jet.shutdownAll();

	}
}
