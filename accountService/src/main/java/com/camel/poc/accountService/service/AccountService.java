package com.camel.poc.accountService.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.camel.poc.bean.Account;
import com.camel.poc.bean.Customer;

@Service
public class AccountService {

	private static List<Account> lstArraylist = new ArrayList<>();

	public void addAccount(Customer customer) {
		lstArraylist.addAll(customer.getAccounts());
	}

	public List<Account> findByNumber(String number) {
		if (null == number || number.trim().isEmpty())
			return lstArraylist;
		return lstArraylist.stream().filter(acc -> acc.getNumber().equalsIgnoreCase(number))
				.collect(Collectors.toList());
	}

	public List<Account> findAll() {
		return lstArraylist;
	}

	public List<Account> findById(List<Account> lst) {
		Set<String> setNumber = new HashSet<>();
		lst.forEach(acc -> setNumber.add(acc.getNumber()));
		return lstArraylist.stream().filter(acc -> setNumber.contains(acc.getNumber())).collect(Collectors.toList());
	}

	public void transaction(Account account) {
		Optional<Account> op = lstArraylist.stream().filter(acc -> acc.getNumber().equals(account.getNumber()))
				.findAny();
		if (op.isPresent()) {
			op.get().setBalance(account.getBalance());
		}
	}
}
