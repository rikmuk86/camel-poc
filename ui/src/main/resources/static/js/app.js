'use strict'

var demoApp = angular.module('demo', []);

/*
 * demoApp.service("service",["$scope", "$http", function($scope, $http) {
 * this.getcustomer = function(url){ return $http.get(url); } }]);
 */

demoApp
		.controller(
				"UIController",
				[
						"$scope",
						"$rootScope",
						"$http",
						function($scope, $rootScope, $http) {

							var baseUrl = "http://localhost:8081/gateway/";
							// $http.defaults.headers.post["Content-Type"] =
							// "application/json";

							if (!$scope.addCust) {
								$scope.addCust = {};
								$scope.addCust.account = [];
								var acc = {};
								$scope.addCust.account.push(acc);

								$scope.addAcc = function() {
									var acc = {};
									$scope.addCust.account.push(acc);
								}
								$scope.delAcc = function() {
									$scope.addCust.account.splice(
											$scope.addCust.accounts.length - 1,
											1);
								}
								$scope.searchCust = function() {
									// var vm = this;
									var url = baseUrl
											+ ("cust" + ($scope.searchCustName ? "/"
													+ $scope.searchCustName
													: ""));
									$http.get(url).success(function(data) {
										$scope.custList = data;
									}).error(function() {
										alert('Some error occured.');
									});
								}

								$scope.saveCust = function() {
									var url = baseUrl + "customer/add";
									$http.post(url, $scope.addCust).success(
											function(data) {
												alert(data);
											}).error(function() {
										alert('Some error occured.');
									});
								}

								$scope.showAccount = function(customer) {
									$scope.accList = customer.account;
								}

								$scope.searchAcc = function() {
									var url = baseUrl
											+ ("acc" + ($scope.searchAccNum ? "/"
													+ $scope.searchAccNum
													: ""));
									$http.get(url).success(function(data) {
										$scope.accList = data;
									}).error(function() {
										alert('Some error occured.');
									});
								}

								$scope.addAccBalance = function(account, type) {
									if (type == 'A') {
										account.balance = account.balance
												+ account.transactionAmount;
									} else {
										var balance = account.balance
												- account.transactionAmount;
										if (balance < 0) {
											alert('Balance amount can not be less than Transaction Amount.');
											return false;
										}
										account.balance = balance;
									}
									var url = baseUrl + ("acc/transact");
									$http.post(url, account).success(
											function(responce) {
												alert("Balance Updated");
											}).error(function() {
										alert('Some error occured.');
									});
								}
							}
						} ]);