package com.trade.imdg.hazelcast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import com.example.mu.domain.Instrument;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.trade.imdg.jet.implementations.JetKafkaConsume;

@SpringBootApplication(scanBasePackages = "com.trade.imdg")
@EnableCaching
public class Main {

	public static final String MAP_INSTRUMENTS = "instrument";
	public final Logger LOG = LoggerFactory.getLogger(Main.class);

	@Bean
	HazelcastInstance hzInstance() {

		return Hazelcast.newHazelcastInstance();
		//return HazelcastClient.newHazelcastClient();

	}
	
	@Bean
	JetInstance jet() {

		//JetConfig config = new JetConfig();
		//config.getInstanceConfig().setCooperativeThreadCount(2);
		
		
		
		//config.setHazelcastConfig(hzInstance.getConfig());
		
		//String groupName = config.getHazelcastConfig().getGroupConfig().getName();
		//LOG.info("Group name is "+groupName);
		
		//JetInstance instance = Jet.newJetInstance(config);
		return Jet.newJetClient();

		//return instance;

	}

	

	//@Autowired
	//private JetInstance jet;

	@Autowired
	private HazelcastInstance hzInstance;

	@PreDestroy
	public void shutDownCache() {
		LOG.info("Shutting down the Instrumment and Party Cache ...");
		//jet.shutdown();
		LOG.info("Completed shutdown");
	}

	@PostConstruct
	private void loadCache() throws Exception {

		try {

			IMap<String, Instrument> instruments = hzInstance
					.getMap(MAP_INSTRUMENTS);
			System.out.println("Size of Instruments is " + instruments.size());
			System.out.println("Loading instruments ...");

			// load the tickers into Map
			// AAL|American Airlines Group, Inc. - Common Stock|Q|N|N|100|N|N
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(Main.class
							.getResourceAsStream("/nasdaqlisted.txt"),
							StandardCharsets.UTF_8))) {

				reader.lines()
						.skip(1)
						// .limit(100)
						.map(l -> Arrays.asList(l.split("\\|")))
						.forEach(
								t -> instruments.put(t.get(0),
										constructInstrument(t)));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			System.out.println("Size of Instruments is " + instruments.size());
			// instruments.loadAll(false);
			
			
			
		} finally {
			// Jet.shutdownAll();
		}

	}

	private Instrument constructInstrument(List<String> instrumentData) {
		Instrument aInstrument = new Instrument();
		aInstrument.setAssetClass(instrumentData.get(0));
		aInstrument.setInstrumentId(instrumentData.get(0));
		aInstrument.setIssuer(instrumentData.get(1));
		aInstrument.setSymbol(instrumentData.get(0));

		return aInstrument;
	}

	public static void main(String[] args) {

		SpringApplication.run(Main.class, args);
		// Runtime.getRuntime().addShutdownHook(new
		// Thread(Main::shutdownCache));
		// SpringApplication.run(Main.class, args).close();

	}
}
