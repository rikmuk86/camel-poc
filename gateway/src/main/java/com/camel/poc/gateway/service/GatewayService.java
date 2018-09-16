package com.camel.poc.gateway.service;

import org.springframework.stereotype.Service;

import com.camel.poc.bean.Account;
import com.camel.poc.bean.Customer;

@Service("gatewayService")
public class GatewayService {
	public void addCustomer(Customer customer) {
		System.out.println("gateway called : customer name : " + customer.getName());
	}

	public void accTransaction(Account account) {
		System.out.println("Account transaction call");
	}
}
