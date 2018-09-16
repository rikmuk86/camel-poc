package com.camel.poc.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "customer")
@XmlAccessorType(XmlAccessType.FIELD)
public class Customer {
	@XmlElement(name = "name")
	private String name;
	@XmlElement(name = "age")
	private int age;
	@XmlElement(name = "gender")
	private String gender;
	@XmlElement(name = "account")
	private List<Account> accounts;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}

	public Customer(String name, int age, String gender, List<Account> accounts) {
		super();
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.accounts = accounts;
	}

	public Customer() {
		super();
	}

}
