package com.trade.injector.schedulers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.mu.domain.Instrument;
import com.example.mu.domain.Party;
import com.example.mu.domain.PositionAccount;
import com.example.mu.domain.Price;
import com.example.mu.domain.Trade;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.trade.injector.business.service.BusinessServiceCacheNames;
import com.trade.injector.business.service.GeneratePriceData;
import com.trade.injector.jto.InstrumentReport;
import com.trade.injector.jto.TradeReport;
import com.trade.injector.jto.repository.TradeReportRepository;
import com.trade.injector.sinks.KafkaSink;

@Component
public class PriceScheduler {

	final Logger LOG = LoggerFactory.getLogger(PriceScheduler.class);
	private static final String REPORT_NAME = "MASTER_REPORT";

	@Autowired
	private KafkaSink sender;

	@Autowired
	HazelcastInstance hazelcastInstance;

	@Value("${kafka.topic.marketData}")
	private String marketDataTopic;

	@Autowired
	private TradeReportRepository reportRepo;

	@Autowired
	private MongoTemplate coreTemplate;

	/**
	 * This scheduler will generate prices every 1 minute for instruments that
	 * currently exist within the cache
	 * 
	 * @throws Exception
	 */

	@Scheduled(fixedDelay = 60000)
	public void generateFrequentPriceData() throws Exception {

		LOG.info("Starting to generate price...");
		// first get all the prices from the cache
		IMap<String, Instrument> mapInstruments = hazelcastInstance
				.getMap("instrument");

		// no data do not generate
		if (mapInstruments.size() == 0) {
			LOG.warn("No instruments found to generate prices");
			return;
		}

		// generate and sink to Kafka
		Collection<Instrument> ins = mapInstruments.values();
		LOG.info("Number of Instruments " + ins.size());
		ins.stream().forEach(
				a -> sender.send(marketDataTopic, GeneratePriceData
						.generateRandomDataOnInstruments(a).toJSON()));

		LOG.info("Price generation done");
	}

	@Scheduled(fixedDelay = 60000)
	public void getUpdatedPriceData() throws Exception {

		TradeReport tradeReport = coreTemplate
				.findOne(
						Query.query(Criteria.where("injectorProfileId").is(
								REPORT_NAME)), TradeReport.class);

		if (tradeReport == null)
			tradeReport = createTradeReport();

		LOG.info("Starting to obtain prices...");
		// first get all the prices from the cache
		IMap<String, Price> mapPrices = hazelcastInstance
				.getMap(BusinessServiceCacheNames.PRICE_CACHE);

		// no data do not generate
		if (mapPrices.size() == 0) {
			LOG.warn("No prices found");
			return;
		}

		List<InstrumentReport> instrumentReport = tradeReport.getInstruments();

		if (instrumentReport.size() == 0)
			tradeReport.setInstruments(mapPrices.values().stream()
					.map(p -> createNewInstrumentReport(p))
					.collect(Collectors.toList()));
		else {
			instrumentReport
					.stream()
					.forEach(x -> updateInstrumentReportPrice(x, mapPrices.get(x.getId())));
		}
		// generate and sink to Kafka
		Collection<Price> px = mapPrices.values();
		LOG.info("Number of prices " + px.size());
		// ins.stream().forEach(a->sender.send(marketDataTopic,GeneratePriceData.generateRandomDataOnInstruments(a).toJSON()));

		reportRepo.save(tradeReport);
		LOG.info("Prices obtained done");
	}

	private TradeReport createTradeReport() throws Exception {

		// initialise the report if not found and create the Instrument report
		// for each price

		// create a new one
		TradeReport tradeReport = new TradeReport();
		tradeReport.setCurrentTradeProgress(1);
		tradeReport.setInjectorProfileId(REPORT_NAME);
		tradeReport.setName("Report_" + REPORT_NAME);
		tradeReport.setReportDate(new Date(System.currentTimeMillis()));
		tradeReport.setTradeCount(1);
		// tradeReport.setUserId(username);

		// //tradeReport.setInstruments(instruments);
		List<InstrumentReport> instrumentList = new ArrayList<InstrumentReport>();
		// instrumentList =
		// prices.stream().map(p->createNewInstrumentReport(p)).collect(Collectors.toList());
		tradeReport.setInstruments(instrumentList);

		// now go through each price and update the Instrument report with the
		// current and previous price
		return tradeReport;

	}

	private InstrumentReport createNewInstrumentReport(Price aPrice) {
		InstrumentReport report = new InstrumentReport();
		report.setId(aPrice.getInstrumentId());
		report.setPrice(aPrice.getPrice());
		report.setPrevPrice(aPrice.getPrice());
		return report;
	}

	private void updateInstrumentReportPrice(InstrumentReport report,
			Price aPrice) {

		report.setPrevPrice(report.getPrice());
		report.setPrice(aPrice.getPrice());

	}

}
