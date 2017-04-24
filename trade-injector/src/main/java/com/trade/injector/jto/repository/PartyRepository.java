package com.trade.injector.jto.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.trade.injector.jto.Party;

public interface PartyRepository extends MongoRepository<Party, String> {
	
	public Party findByPartyId(String partyId);

}
