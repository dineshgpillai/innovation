angular
		.module("TradeInjectorApp.controllers")
		.controller(
				"TradeInjectCtrl",
				function($scope, $http, $location, TradeInjectorService) {

					$scope.tradeAcks = [];
					// $scope.tradeInjectorMessage = [];
					$scope.labels = [];
					$scope.data = [];
					$scope.labels_instrument = [];
					$scope.instrumentCount = [];
					$scope.clientCount = [];
					$scope.showGeneration = false;
					$scope.totalMsgCount = [ 0 ];
					$scope.tab = 1;
					$scope.user = [];
					$scope.authenticated = false;

					$scope.setTab = function(newTab) {
						$scope.tab = newTab;
					}

					$scope.isSet = function(tabNum) {
						return $scope.tab === tabNum;
					}

					// login
					$http.get("/user").success(function(data) {
						$scope.user = data.userAuthentication.details.name;
						$scope.authenticated = true;
					}).error(function() {
						$scope.user = "N/A";
						$scope.authenticated = false;
					});

					// logout
					$scope.logout = function() {
						$http.post('/logout', {}).success(function() {
							$scope.authenticated = false;
							$location.path("/");
						}).error(function(data) {
							console.log("Logout failed")
							$scope.authenticated = false;
						});
					};

					$scope.options = {
						responsive : true,
						responsiveAnimationDuration : 1000,
						title : {
							display : true,
							text : 'Trade Count by Client Chart'
						}
					};

					$scope.options_instrument = {
						responsive : true,
						responsiveAnimationDuration : 1000,
						title : {
							display : true,
							text : 'Trade Count by Instrument Chart'
						}
					};

					$scope.datasetOverride = {
						fill : false
					};

					$scope.injectTrades = function() {
						$scope.tradeAcks = [];
						$scope.labels = [];
						$scope.data = [];
						$scope.labels_instrument = [];
						$scope.instrumentCount = [];
						$scope.clientCount = [];
						$scope.totalMsgCount = [ 0 ];
						console.log('Before sending '
								+ $scope.tradeInjectorMessage);
						TradeInjectorService.send($scope.tradeInjectorMessage);
					};

					// Receives the trade ack and summarises the trade count
					TradeInjectorService
							.receive()
							.then(

									null,
									null,
									function(data) {
										$scope.showGeneration = true;
										$scope.tradeAcks.push(data);
										var clientNameIndex = $scope.labels
												.indexOf(data.clientName)

										if (clientNameIndex == -1) {
											// this is a new client set the
											// trade count
											// appropriately
											$scope.clientCount.push(1);
											$scope.labels.push(data.clientName);
										} else {
											// existing client, update the trade
											// count
											$scope.clientCount
													.splice(
															clientNameIndex,
															1,
															$scope.clientCount[clientNameIndex] + 1);
											$scope.labels.splice(
													clientNameIndex, 1,
													data.clientName);
										}

										// do the same for instruments
										var instrumentIdIndex = $scope.labels_instrument
												.indexOf(data.instrumentId)

										if (instrumentIdIndex == -1) {
											$scope.instrumentCount.push(1);
											$scope.labels_instrument
													.push(data.instrumentId);
										} else {
											$scope.instrumentCount
													.splice(
															instrumentIdIndex,
															1,
															$scope.instrumentCount[instrumentIdIndex] + 1);
											$scope.labels_instrument.splice(
													instrumentIdIndex, 1,
													data.instrumentId)
										}

										// increment the msg count
										$scope.totalMsgCount.splice(0, 1,
												$scope.totalMsgCount[0] + 1);

																				

									});

				}

		)
		.controller(
				"TradeInjectTableDisplay",
				function($scope, $http, $location, TradeInjectorService) {

					$http.get("/user").success(function(data) {
						$scope.user = data.userAuthentication.details.name;
						$scope.authenticated = true;
					}).error(function() {
						$scope.user = "N/A";
						$scope.authenticated = false;
					});

					$scope.tradeInjectMessages = [];

					// ensure all messages are retrieved first
					$http.get("/retrieveAllInjects").success(function(data) {
						$scope.tradeInjectMessages = data;
					});

					$scope.stop = function(injectId) {
						var data = $.param({
							id : injectId
						});

						var config = {
							headers : {
								'Content-Type' : 'application/x-www-form-urlencoded;charset=utf-8;'
							}
						}

						$http.post('/tradeMessageStop', data, config).success(
								function(data, status, headers, config) {
									$scope.PostDataResponse = data;
									$scope.showGeneration = false;
								}).error(
								function(data, status, header, config) {
									$scope.ResponseDetails = "Data: " + data
											+ "<hr />status: " + status
											+ "<hr />headers: " + header
											+ "<hr />config: " + config;
								});

						// $http.post('/tradeMessageStop');

					}

					$scope.repeat = function(injectId) {
						var data = $.param({
							id : injectId
						});

						var config = {
							headers : {
								'Content-Type' : 'application/x-www-form-urlencoded;charset=utf-8;'
							}
						}

						$http
								.post('/tradeMessageRepeat', data, config)
								.success(
										function(data, status, headers, config) {
											$scope.PostDataResponse = data;
											$scope.showGeneration = false;
										}).error(
										function(data, status, header, config) {
											$scope.ResponseDetails = "Data: "
													+ data + "<hr />status: "
													+ status
													+ "<hr />headers: "
													+ header + "<hr />config: "
													+ config;
										});

					}
					
					$scope.play = function(injectId) {
						var data = $.param({
							id : injectId
						});

						var config = {
							headers : {
								'Content-Type' : 'application/x-www-form-urlencoded;charset=utf-8;'
							}
						}

						$http
								.post('/tradeMessagePlay', data, config)
								.success(
										function(data, status, headers, config) {
											$scope.PostDataResponse = data;
											$scope.showGeneration = false;
										}).error(
										function(data, status, header, config) {
											$scope.ResponseDetails = "Data: "
													+ data + "<hr />status: "
													+ status
													+ "<hr />headers: "
													+ header + "<hr />config: "
													+ config;
										});

					}


					$scope.purgeAll = function() {
						$http
								.post('/purgeAllInjects')
								.success(
										function(data) {
											// refresh the table list
											$http
													.get("/retrieveAllInjects")
													.success(
															function(data) {

																var newData = data
																		.slice(0);
																$scope.tradeInjectMessages.length = 0
																$scope.tradeInjectMessages.push
																		.apply(
																				$scope.tradeInjectMessages,
																				newData);
															});
										});

					};

					// refresh the entire table
					$scope.refreshAll = function() {

						$http.get("/retrieveAllInjects").success(
								function(data) {

									$scope.tradeInjectMessages = data;
								});

					};

					// Receives the trade inject messages
					TradeInjectorService.receiveTradeInjectMessage().then(

					null, null, function(data) {
						$scope.tradeInjectMessages = data;
					});
				});
