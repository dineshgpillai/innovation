package com.trade.injector.jto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TradeInjectorProfile {

	@Id
	public String id;
	
	@Indexed
	private String name;
	
	
	private int threads;
	private int numberOfTrades;
	private long simulatedWaitTime;
	private int numberOfParties;
	private List<String> parties = new ArrayList<String>();	
	private int numberOfInstruments;
	private List<String> instruments = new ArrayList<String>();
	private String exchange_id;
	private Date tradeDate;
	private double minPxRange;
	private double maxPxRange;
	private int minQtyRange;
	private int maxQtyRange;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getThreads() {
		return threads;
	}
	public void setThreads(int threads) {
		this.threads = threads;
	}
	public int getNumberOfTrades() {
		return numberOfTrades;
	}
	public void setNumberOfTrades(int numberOfTrades) {
		this.numberOfTrades = numberOfTrades;
	}
	public long getSimulatedWaitTime() {
		return simulatedWaitTime;
	}
	public void setSimulatedWaitTime(long simulatedWaitTime) {
		this.simulatedWaitTime = simulatedWaitTime;
	}
	public int getNumberOfParties() {
		return numberOfParties;
	}
	public void setNumberOfParties(int numberOfParties) {
		this.numberOfParties = numberOfParties;
	}
	public int getNumberOfInstruments() {
		return numberOfInstruments;
	}
	public void setNumberOfInstruments(int numberOfInstruments) {
		this.numberOfInstruments = numberOfInstruments;
	}
	public List<String> getParties() {
		return parties;
	}
	public void setParties(List<String> parties) {
		this.parties = parties;
	}
	public List<String> getInstruments() {
		return instruments;
	}
	public void setInstruments(List<String> instruments) {
		this.instruments = instruments;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getExchange_id() {
		return exchange_id;
	}
	public void setExchange_id(String exchange_id) {
		this.exchange_id = exchange_id;
	}
	public Date getTradeDate() {
		return tradeDate;
	}
	public void setTradeDate(Date tradeDate) {
		this.tradeDate = tradeDate;
	}
	public double getMinPxRange() {
		return minPxRange;
	}
	public void setMinPxRange(double minPxRange) {
		this.minPxRange = minPxRange;
	}
	public double getMaxPxRange() {
		return maxPxRange;
	}
	public void setMaxPxRange(double maxPxRange) {
		this.maxPxRange = maxPxRange;
	}
	public int getMinQtyRange() {
		return minQtyRange;
	}
	public void setMinQtyRange(int minQtyRange) {
		this.minQtyRange = minQtyRange;
	}
	public int getMaxQtyRange() {
		return maxQtyRange;
	}
	public void setMaxQtyRange(int maxQtyRange) {
		this.maxQtyRange = maxQtyRange;
	}
	
	
	
}
