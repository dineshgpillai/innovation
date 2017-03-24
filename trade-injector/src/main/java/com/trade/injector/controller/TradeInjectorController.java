package com.trade.injector.controller;

import java.util.List;

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

@RestController
@RequestMapping("/tradeInject")
public class TradeInjectorController {

	//@Autowired
	//private SimpMessagingTemplate messageSender;

	@Autowired
	GenerateTradeData tradeData;

	//@MessageMapping("/tradeMessageInject")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> tradeInject(@RequestBody TradeInjectorMessage message) throws Exception {

		int numberOfTrades = new Integer(message.getNoOfTrades());
		int numberOfClients = new Integer(message.getNoOfClients());
		int numberOfInstruments = new Integer(message.getNoOfInstruments());

		if (numberOfTrades < 1 || numberOfClients < 1
				|| numberOfInstruments < 1)
			throw new Exception(
					"Minimum 1 must be specified for Trades, Clients and Instruments");
		List <Instrument> listOfInstruments = new GenerateRandomInstruments().createRandomData(numberOfInstruments);
		List <Party> listOfParties = new GenerateRandomParty().createRandomData(numberOfClients);
		
		for (int i = 0; i < numberOfTrades; i++) {
				Trade aTrade = tradeData.createTradeData(i, listOfParties, listOfInstruments);
				System.out.println("Following trade was generated "+aTrade.toString());
				
				
		}

		//TradeAcknowledge ack = new TradeAcknowledge();
		//messageSender.convertAndSend("/topic/tradeAck/", ack);
		return ResponseEntity.ok().build();
	}

}
