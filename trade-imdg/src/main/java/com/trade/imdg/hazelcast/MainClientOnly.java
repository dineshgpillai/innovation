package com.trade.imdg.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

public class MainClientOnly {
	
	public static void main(String args[]){
		System.out.println("Starting client...");
		HazelcastInstance client = HazelcastClient.newHazelcastClient();
	}
	

}
