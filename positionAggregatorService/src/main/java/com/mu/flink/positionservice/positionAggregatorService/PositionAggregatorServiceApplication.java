package com.mu.flink.positionservice.positionAggregatorService;

import java.util.List;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.quickstart.WordCount.LineSplitter;

import org.apache.flink.util.Collector;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import akka.remote.RemoteWatcher.Stats;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientAwsConfig;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.GroupProperty;
import com.hazelcast.zookeeper.ZookeeperDiscoveryProperties;
import com.hazelcast.zookeeper.ZookeeperDiscoveryStrategy;
import com.hazelcast.zookeeper.ZookeeperDiscoveryStrategyFactory;

//@SpringBootApplication
public class PositionAggregatorServiceApplication {

	public static final String FLINK_CLUSTER_HOST = "172.19.0.3";
	public static final int FLINK_CLUSTER_PORT = 6123;
	public static final String ZK_HOST = "138.68.168.237"
	,                          ZK_HOST_DOCKER = "zookeeper-1.vnet";
	public static final String ZK_HZ_PATH = "/discovery/hazelcast";

	// create static instance for zookeeper class.
	private static ZooKeeper zk;

	// create static instance for ZooKeeperConnection class.
	private static ZooKeeperConnection conn;

	public static void connectToZookeeper() throws Exception {
		try {
			conn = new ZooKeeperConnection();
			zk = conn.connect(ZK_HOST);
		} finally {

		}

	}
	
	public static ClientConfig getClientHZConfig(){
		ClientConfig config = new ClientConfig();
		
		ClientNetworkConfig nwConfig = new ClientNetworkConfig();
		ClientAwsConfig clientAwsConfig = new ClientAwsConfig();
		clientAwsConfig.setEnabled(false);
		nwConfig.setAwsConfig(clientAwsConfig);
		config.setNetworkConfig(nwConfig);
	    //config.getNetworkConfig().getAwsConfig().setEnabled(false);
	    config.setProperty(GroupProperty.DISCOVERY_SPI_ENABLED.getName(), "true");

	    //ZookeeperDiscoveryStrategy zkp =  new ZookeeperDiscoveryStrategy(null, null, null);
	    //zkp.discoverNodes();
	    DiscoveryStrategyConfig discoveryStrategyConfig= new DiscoveryStrategyConfig("com.hazelcast.zookeeper.ZookeeperDiscoveryStrategy");
	    //DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(new ZookeeperDiscoveryStrategyFactory());
	    discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.ZOOKEEPER_URL.key(), ZK_HOST+":2181,"+ZK_HOST_DOCKER+":2181");
	    discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.ZOOKEEPER_PATH.key(), ZK_HZ_PATH);
	    discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.GROUP.key(), "kappa-serving-layer");
	    config.getNetworkConfig().getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);

		
		return config;
	}

	// 
	public static void main(String[] args) throws Exception {
		// SpringApplication.run(PositionAggregatorServiceApplication.class,
		// args);

		System.out.println("Hello");
		try {

			connectToZookeeper();
			
			Stat stats = zk.exists(ZK_HZ_PATH, true);
			
			System.out.println("Path to Zookeeper Hazelcasts exists? "
					+stats );
			
			List<ACL> acls = zk.getACL(ZK_HZ_PATH, stats);
			
			for(ACL aCL : acls){
				System.out.println(aCL.toString());
			}
			
			byte[] data = zk.getData(ZK_HZ_PATH, true, stats);
			String d = new String(data, "UTF-8");
            System.out.println("Data from zoo "+d);

			
			//HazelcastClient.newHazelcastClient(getClientHZConfig());
            Hazelcast.newHazelcastInstance();
			
			final ExecutionEnvironment env = ExecutionEnvironment
					.getExecutionEnvironment();
			
			//HazelcastInstance hazelcastInstance =
					//HazelcastClient.newHazelcastClient();
			
			env.getConfig().enableSysoutLogging();
			// final ExecutionEnvironment env =
			// ExecutionEnvironment.createRemoteEnvironment(FLINK_CLUSTER_HOST,
			// FLINK_CLUSTER_PORT, "");
			// get input data
			DataSet<String> text = env.fromElements(
					"To be, or not to be,--that is the question:--",
					"Whether 'tis nobler in the mind to suffer",
					"The slings and arrows of outrageous fortune",
					"Or to take arms against a sea of troubles,");

			DataSet<Tuple2<String, Integer>> counts =
			// split up the lines in pairs (2-tuples) containing: (word,1)
			text.flatMap(new LineSplitter())
			// group by the tuple field "0" and sum up tuple field "1"
					.groupBy(0).sum(1);

			// execute and print result
			counts.print();

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(conn != null)
				conn.close();
		}

	}

	public static final class LineSplitter implements
			FlatMapFunction<String, Tuple2<String, Integer>> {

		@Override
		public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
			// normalize and split the line
			String[] tokens = value.toLowerCase().split("\\W+");

			// emit the pairs
			for (String token : tokens) {
				if (token.length() > 0) {
					out.collect(new Tuple2<String, Integer>(token, 1));
				}
			}
		}
	}
}
