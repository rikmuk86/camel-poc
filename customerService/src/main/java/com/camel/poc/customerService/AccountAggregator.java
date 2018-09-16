package com.camel.poc.customerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import com.camel.poc.bean.Account;
import com.camel.poc.bean.Customer;

public class AccountAggregator implements AggregationStrategy {

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

		Object originalBody = oldExchange.getIn().getBody();

		Object resourceResponse = newExchange.getIn().getBody();

		List<Customer> lstCustomer = (List<Customer>) originalBody;
		List<Account> lstAcc = (List<Account>) resourceResponse;

		Map<String, Account> map = new HashMap<>();
		lstAcc.forEach(acc -> map.put(acc.getNumber(), acc));

		lstCustomer.forEach(cust -> {
			cust.getAccounts().forEach(acc -> {
				acc.setBalance(map.get(acc.getNumber()).getBalance());
			});
		});

		oldExchange.getIn().setBody(lstCustomer);

		return oldExchange;

	}

}
