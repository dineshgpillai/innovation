package com.trade.injector.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.trade.injector.jto.TradeAcknowledge;
import com.trade.injector.jto.TradeInjectorMessage;

@Controller
public class TradeInjectorController {

	@Autowired
	private SimpMessagingTemplate messageSender;

	@MessageMapping("/tradeMessageInject")
	public void tradeInject(TradeInjectorMessage message) throws Exception {
		Thread.sleep(1000); // simulated delay

		TradeAcknowledge ack = new TradeAcknowledge();
		messageSender.convertAndSend("/topic/tradeAck/", ack);
	}

}
