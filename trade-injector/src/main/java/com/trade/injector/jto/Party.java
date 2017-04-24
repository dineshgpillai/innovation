package com.trade.injector.jto;



import org.springframework.data.annotation.Id;

import com.trade.injector.enums.PartyRole;

public class Party {

	@Id
	public String id;
	
	private String partyId;
	private String partyName;
	private String accountNumber;
	private String subAccount;
	private PartyRole role;
	private Party immediateParent;
	private Party rootParent;

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public String getPartyName() {
		return partyName;
	}

	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}

	public PartyRole getRole() {
		return role;
	}

	public void setRole(PartyRole role) {
		this.role = role;
	}

	public Party getImmediateParent() {
		return immediateParent;
	}

	public void setImmediateParent(Party immediateParent) {
		this.immediateParent = immediateParent;
	}

	public Party getRootParent() {
		return rootParent;
	}

	public void setRootParent(Party rootParent) {
		this.rootParent = rootParent;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getSubAccount() {
		return subAccount;
	}

	public void setSubAccount(String subAccount) {
		this.subAccount = subAccount;
	}

	

}
