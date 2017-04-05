package com.trade.injector.jto;

import org.springframework.data.annotation.Id;


public class TradeInjectorMessage {
	
	@Id
    public String id;
	
	private String userId;
	private String noOfTrades;
	private String noOfClients;
	private String noOfInstruments;
	private String timeDelay;
	private String tradeDate;
	private InjectorProfile profileUsed;
	
	public String getNoOfTrades() {
		return noOfTrades;
	}
	public void setNoOfTrades(String noOfTrades) {
		this.noOfTrades = noOfTrades;
	}
	public String getNoOfClients() {
		return noOfClients;
	}
	public void setNoOfClients(String noOfClients) {
		this.noOfClients = noOfClients;
	}
	public String getNoOfInstruments() {
		return noOfInstruments;
	}
	public void setNoOfInstruments(String noOfInstruments) {
		this.noOfInstruments = noOfInstruments;
	}
	public String getTimeDelay() {
		return timeDelay;
	}
	public void setTimeDelay(String timeDelay) {
		this.timeDelay = timeDelay;
	}
	public String getTradeDate() {
		return tradeDate;
	}
	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public InjectorProfile getProfileUsed() {
		return profileUsed;
	}
	public void setProfileUsed(InjectorProfile profileUsed) {
		this.profileUsed = profileUsed;
	}
	
	@Override
    public String toString() {
        return String.format(
                "TradeInjector[id=%s, userId='%s', noOfTrades='%s', noOfClients='%s', noOfInstruments='%s']",
                id, userId, noOfTrades, noOfClients, noOfInstruments);
    }


}
