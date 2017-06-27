package org.apache.flink.quickstart;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.example.mu.domain.Price;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class KafkaConnect {
	
	private static Gson gson = new GsonBuilder().create();
	private static HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient();

	public Properties consumerConfigs() {
		Properties props = new Properties();
		// list of host:port pairs used for establishing the initial connections
		// to the Kakfa cluster
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				"138.68.168.237:9092");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				JsonDeserializer.class);
		// allows a pool of processes to divide the work of consuming and
		// processing records
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "mu");

		return props;
	}

	public void connectToMarketData() throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment();
		

		DataStream<String> stream = env.addSource(new FlinkKafkaConsumer010(
				"market_data", new SimpleStringSchema(), consumerConfigs()));

		stream.map(new MapFunction<String, Price>() {
			private static final long serialVersionUID = -6867736771747690202L;
			
			
			//returns a price object
			@Override
			public Price map(String value) throws Exception {
				
					
		            Price p = gson.fromJson(value, Price.class);

				return p;
			}
		}).map(new MapFunction<Price,String>(){

			//stores the price object in hazelcast
			@Override
			public String map(Price arg0) throws Exception {
				
				
				IMap<String, Price> priceMap = hazelcastInstance.getMap("price");
				priceMap.put(arg0.getInstrumentId(), arg0);
				return "Price stored in hz "+arg0.getInstrumentId();
			}
			
		});
		

		env.execute();

	}
	
	public static void main(String[]args) throws Exception{
		new KafkaConnect().connectToMarketData();
	}

}
