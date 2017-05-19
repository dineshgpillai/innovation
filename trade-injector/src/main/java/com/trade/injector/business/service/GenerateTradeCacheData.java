package com.trade.injector.business.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.example.mu.domain.Instrument;
import com.example.mu.domain.Party;
import com.example.mu.domain.Trade;

public class GenerateTradeCacheData {
	
	public Trade[] createTrade(int number, Map<String, Party> parties, Map<String, Instrument> instruments) throws Exception{
	
		//generate all the common attributes for both sides of the trade
		Trade[] matchedTrade = new Trade[2];
		String executionId = UUID.randomUUID().toString();
		String currency = "USD";
		String executingFirmId = "TEST_EX1_"+number;
		String executingTraderId = "TEST_TRD1";
		String executionVenue = "EX1";
		double price = (double) (Math.random() * 1000 + 1);
		int quantity = (int) (Math.random() * 100 + 1);
		
		Party buyParty = getRandomParty(parties);
		Party sellParty = getRandomParty(parties);
		Date tradeDate = new Date(System.currentTimeMillis());
		while(buyParty.equals(sellParty)){
			//keep looping till we get a different party
			sellParty = getRandomParty(parties);
		}
		Instrument tradedInstrument = getRandomInstrument(instruments);
		//do the buy
		String buyKey = UUID.randomUUID().toString();
		Trade atrade = new Trade();
		atrade.setClientId(buyParty.getPartyId());
		atrade.setCurrency(currency);
		atrade.setExecutingFirmId(executingFirmId);
		atrade.setExecutingTraderId(executingTraderId);
		atrade.setExecutionId(buyKey);
		atrade.setExecutionVenueId(executionVenue);
		atrade.setFirmTradeId(executionId);
		atrade.setInstrumentId(tradedInstrument.getSymbol());
		atrade.setOriginalTradeDate(tradeDate);
		atrade.setPositionAccountId(buyParty.getPositionAccountId());
		atrade.setPrice(price);
		atrade.setQuantity(quantity);
		atrade.setSecondaryFirmTradeId(executionId);
		atrade.setSecondaryTradeId(executionId);
		atrade.setSettlementDate(tradeDate);
		atrade.setTradeDate(tradeDate);
		atrade.setTradeId(executionId);
		atrade.setTradeType("0");

		//trade.put(buyKey, atrade);
		
		//now generate Sell side
		String sellKey = UUID.randomUUID().toString();
		Trade aSelltrade = new Trade();
		aSelltrade.setClientId(sellParty.getPartyId());
		aSelltrade.setCurrency(currency);
		aSelltrade.setExecutingFirmId(executingFirmId);
		aSelltrade.setExecutingTraderId(executingTraderId);
		aSelltrade.setExecutionId(sellKey);
		aSelltrade.setExecutionVenueId(executionVenue);
		aSelltrade.setFirmTradeId(executionId);
		aSelltrade.setInstrumentId(tradedInstrument.getSymbol());
		aSelltrade.setOriginalTradeDate(tradeDate);
		aSelltrade.setPositionAccountId(sellParty.getPositionAccountId());
		aSelltrade.setPrice(price);
		aSelltrade.setQuantity(-quantity);
		aSelltrade.setSecondaryFirmTradeId(executionId);
		aSelltrade.setSecondaryTradeId(executionId);
		aSelltrade.setSettlementDate(tradeDate);
		aSelltrade.setTradeDate(tradeDate);
		aSelltrade.setTradeId(executionId);
		aSelltrade.setTradeType("0");

		//trade.put(sellKey, aSelltrade);
		matchedTrade[0] = atrade;
		matchedTrade[1] = aSelltrade;
		
		return matchedTrade;

		
	}
	
	private Party getRandomParty(Map<String, Party> partyList) throws Exception {
		if (partyList.isEmpty())
			return null;
		else{
			List<String> keysAsArrays = new ArrayList<String>(partyList.keySet());
			Random r = new Random();
			return partyList.get(keysAsArrays.get(r.nextInt(keysAsArrays.size())));
		}
			

	}

	private Instrument getRandomInstrument(Map<String, Instrument> insList)
			throws Exception {
		if (insList.isEmpty())
			return null;
		else{
			List<String> keysAsArrays = new ArrayList<String>(insList.keySet());
			Random r = new Random();
			return insList.get(keysAsArrays.get(r.nextInt(keysAsArrays.size())));
		}
			

	}


}
