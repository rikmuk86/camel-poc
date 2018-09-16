package com.camel.poc.customerService.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.camel.poc.bean.Customer;

@Service
public class CustomerService {
	private static ArrayList<Customer> customers = new ArrayList<>();

	public void addCustomer(Customer customer) {
		System.out.println("Customer service called : customer name : " + customer.getName());
		customers.add(customer);
	}

	public List<Customer> findAll() {
		return customers;
	}

	public List<Customer> findByName(String name) {
		if (null == name || name.trim().isEmpty()) {
			return customers;
		} else
			return customers.stream().filter(cust -> cust.getName().toLowerCase().indexOf(name) != -1)
					.collect(Collectors.toList());
	}
	public List<Customer> returnAll(List<Customer> lst){
		return lst;
	}
}
