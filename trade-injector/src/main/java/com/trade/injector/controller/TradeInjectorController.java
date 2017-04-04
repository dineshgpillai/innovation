package com.trade.injector.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.trade.injector.business.service.GenerateRandomInstruments;
import com.trade.injector.business.service.GenerateRandomParty;
import com.trade.injector.business.service.GenerateTradeData;
import com.trade.injector.dto.Instrument;
import com.trade.injector.dto.Party;
import com.trade.injector.dto.Trade;
import com.trade.injector.jto.TradeAcknowledge;
import com.trade.injector.jto.TradeInjectorMessage;
import com.trade.injector.jto.repository.MongoDBTemplate;

@RestController
@RequestMapping("/")
public class TradeInjectorController {

	final Logger LOG = LoggerFactory.getLogger(TradeInjectorController.class);

	@Autowired
	private SimpMessagingTemplate messageSender;
	
	@Autowired
	private MongoDBTemplate template;

	@Autowired
	GenerateTradeData tradeData;
	
	private static boolean isKill = false;
	
	@RequestMapping(value="/tradeMessageStop",method = RequestMethod.POST)
	public void tradeStop(){
		isKill=true;
	}

	@MessageMapping("/tradeMessageInject")
	// @RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> tradeInject(
			@RequestBody TradeInjectorMessage message) throws Exception {

		isKill = false;
		int numberOfTrades = new Integer(message.getNoOfTrades());
		int numberOfClients = new Integer(message.getNoOfClients());
		int numberOfInstruments = new Integer(message.getNoOfInstruments());
		int timedelay = 0;

		if (message.getTimeDelay() != null)
			timedelay = new Integer(message.getTimeDelay());
		else {
			LOG.debug("Time Delay is not set, defaulting to 1000ms");
			timedelay = 1000;
		}

		if (numberOfTrades < 1 || numberOfClients < 1
				|| numberOfInstruments < 1)
			throw new Exception(
					"Minimum 1 must be specified for Trades, Clients and Instruments");
		List<Instrument> listOfInstruments = new GenerateRandomInstruments()
				.createRandomData(numberOfInstruments);
		List<Party> listOfParties = new GenerateRandomParty()
				.createRandomData(numberOfClients);

		for (int i = 0; i < numberOfTrades; i++) {

			Trade aTrade = tradeData.createTradeData(i, listOfParties,
					listOfInstruments);
			TradeAcknowledge ack = convertToAck(aTrade);
			messageSender.convertAndSend("/topic/tradeAck", ack);
			LOG.debug(("Following trade was generated " + aTrade.toString()));
			LOG.debug("Sleeping for " + timedelay + " ms");
			Thread.sleep(timedelay);
			
			//if the kill flag is set by the UI return the process.
			if(isKill)
				return ResponseEntity.ok().build();

		}

		// TradeAcknowledge ack = new TradeAcknowledge();
		// messageSender.convertAndSend("/topic/tradeAck/", ack);
		return ResponseEntity.ok().build();
	}

	private TradeAcknowledge convertToAck(Trade aTrade) {
		TradeAcknowledge ack = new TradeAcknowledge();
		if (aTrade != null) {
			ack.setClientName(aTrade.getClientName());
			ack.setInstrumentId(aTrade.getInstrumentId());
			ack.setSide(aTrade.getSide());
			ack.setTradeDate(aTrade.getTradeDate().toString());
			ack.setTradePx(new Double(aTrade.getTradePx()).toString());
			ack.setTradeQty(new Integer(aTrade.getTradeQty()).toString());
		}

		return ack;
	}

}
