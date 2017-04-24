package com.trade.injector.jto;

import com.trade.injector.enums.ExerciseStyle;
import com.trade.injector.enums.PutCall;
import com.trade.injector.enums.SecurityType;
import com.trade.injector.enums.SettlementMethod;

public class Instrument {

	private String idenfitifier;
	private SecurityType securityType;
	private PutCall putCall;
	private String MMY;
	private String maturityDate;
	private String strikePx;
	private SettlementMethod settMethod;
	private ExerciseStyle exerciseStyle;
	private Party exchange;

	public String getIdenfitifier() {
		return idenfitifier;
	}

	public void setIdenfitifier(String idenfitifier) {
		this.idenfitifier = idenfitifier;
	}

	public SecurityType getSecurityType() {
		return securityType;
	}

	public void setSecurityType(SecurityType securityType) {
		this.securityType = securityType;
	}

	public PutCall getPutCall() {
		return putCall;
	}

	public void setPutCall(PutCall putCall) {
		this.putCall = putCall;
	}

	public String getMMY() {
		return MMY;
	}

	public void setMMY(String mMY) {
		MMY = mMY;
	}

	public String getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(String maturityDate) {
		this.maturityDate = maturityDate;
	}

	public String getStrikePx() {
		return strikePx;
	}

	public void setStrikePx(String strikePx) {
		this.strikePx = strikePx;
	}

	public Party getExchange() {
		return exchange;
	}

	public void setExchange(Party exchange) {
		this.exchange = exchange;
	}

	public SettlementMethod getSettMethod() {
		return settMethod;
	}

	public void setSettMethod(SettlementMethod settMethod) {
		this.settMethod = settMethod;
	}

	public ExerciseStyle getExerciseStyle() {
		return exerciseStyle;
	}

	public void setExerciseStyle(ExerciseStyle exerciseStyle) {
		this.exerciseStyle = exerciseStyle;
	}

	
}
