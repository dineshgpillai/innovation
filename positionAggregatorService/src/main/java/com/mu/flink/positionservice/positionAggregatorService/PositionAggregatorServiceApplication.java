package com.mu.flink.positionservice.positionAggregatorService;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;



//@SpringBootApplication
public class PositionAggregatorServiceApplication {

	private static HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient();
	public static void main(String[] args) {
		//SpringApplication.run(PositionAggregatorServiceApplication.class, args);
		System.out.println("Hello");
	}
}
