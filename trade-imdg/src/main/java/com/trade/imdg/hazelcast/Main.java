package com.trade.imdg.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class Main {
	public static void main(String[] args) {
		HazelcastInstance hz = Hazelcast.newHazelcastInstance();
	    //HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();
		IQueue<String> q = hz.getQueue("q");
	}
}
