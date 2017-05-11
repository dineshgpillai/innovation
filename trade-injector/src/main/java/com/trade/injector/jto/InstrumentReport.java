package com.trade.injector.jto;

public class InstrumentReport {
	
	private String id;
	private String name;
	private int currentTradeCount;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCurrentTradeCount() {
		return currentTradeCount;
	}
	public void setCurrentTradeCount(int currentTradeCount) {
		this.currentTradeCount = currentTradeCount;
	}
	
	public InstrumentReport incrementCountByOne(){
		this.currentTradeCount++;
		return this;
	}


}
