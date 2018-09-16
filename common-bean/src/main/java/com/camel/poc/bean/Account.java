package com.camel.poc.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "account")
@XmlAccessorType(XmlAccessType.FIELD)
public class Account {
	@XmlElement(name = "number")
	private String number;
	@XmlElement(name = "balance")
	private double balance;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public Account(String number, double balance) {
		super();
		this.number = number;
		this.balance = balance;
	}

	public Account() {
		super();
	}

}
