package com.trade.injector.jto;

public enum TradeInjectRunModes {
	
	RUNNING(0), SUSPENDED(1), COMPLETED(2);
	
	private int runMode;
	
	private TradeInjectRunModes(int p_runMode){
		runMode=p_runMode;
	}
	
	public int getRunMode(){
		return runMode;	
		
		
	}

}
