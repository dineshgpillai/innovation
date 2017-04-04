package com.trade.injector.jto.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.trade.injector.jto.TradeInjectorMessage;


public interface TradeInjectorMessageRepository extends MongoRepository<TradeInjectorMessage, String> {
	
	public TradeInjectorMessage findByUserId(String userId);
	
}
