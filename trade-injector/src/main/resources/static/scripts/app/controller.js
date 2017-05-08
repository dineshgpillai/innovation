angular
		.module("TradeInjectorApp.controllers",
				[ 'angularModalService', 'ngAnimate' ])
		.controller(
				"TradeInjectCtrl",
				function($scope, $http, $location, TradeInjectorService,
						ModalService) {

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
						// set the user id
						$scope.tradeInjectorMessage.userId = $scope.user
						TradeInjectorService.send($scope.tradeInjectorMessage);
					};

					// show the angular window
					$scope.showCreateProfile = function(injectid) {

						ModalService.showModal({
							templateUrl : '/createNewProfile.html',
							controller : "ModalCreateNewController",
							inputs : {
								injectId : injectid
							}

						}).then(function(modal) {
							modal.element.modal();
							modal.close.then(function(result) {
								$scope.message = "You said " + result;
							});
						});

					};

				}

		)
		.controller(
				"TradeInjectTableDisplay",
				function($scope, $http, $location, TradeInjectorService,
						ModalService) {

					$scope.user = [];
					$scope.authenticated = false;

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

						$http.post('/tradeMessagePlay', data, config).success(
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

					// show the angular window
					$scope.showTable = function(injectid) {

						ModalService.showModal({
							templateUrl : '/showTableData.html',
							controller : "ModalTableController",
							inputs : {
								injectId : injectid
							}

						}).then(function(modal) {
							modal.element.modal();
							modal.close.then(function(result) {
								$scope.message = "You said " + result;
							});
						});

					};
				})
		.controller(
				'ModalTableController',
				[
						'$scope',
						'$element',
						'injectId',
						'close',
						'TradeInjectorService',
						'filterFilter',
						function($scope, $element, injectId, close,
								TradeInjectorService, filterFilter) {

							$scope.injectId = injectId;
							$scope.tradeAcks = [];
							$scope.labels = [];
							$scope.data = [];
							$scope.labels_instrument = [];
							$scope.instrumentCount = [];
							$scope.clientCount = [];
							$scope.totalMsgCount = [ 0 ];

							TradeInjectorService
									.receive()
									.then(

											null,
											null,
											function(data) {

												console
														.log("data received "
																+ data.injectIdentifier);
												console.log("inject id "
														+ injectId);

												if (injectId === data.injectIdentifier) {
													console
															.log("Yes we have got the right inject id");
													$scope.tradeAcks.push(data);
													var clientNameIndex = $scope.labels
															.indexOf(data.clientName)

													if (clientNameIndex == -1) {
														// this is a new client
														// set
														// the
														// trade count
														// appropriately
														$scope.clientCount
																.push(1);
														$scope.labels
																.push(data.clientName);
													} else {
														// existing client,
														// update
														// the trade
														// count
														$scope.clientCount
																.splice(
																		clientNameIndex,
																		1,
																		$scope.clientCount[clientNameIndex] + 1);
														$scope.labels
																.splice(
																		clientNameIndex,
																		1,
																		data.clientName);
													}

													// do the same for
													// instruments
													var instrumentIdIndex = $scope.labels_instrument
															.indexOf(data.instrumentId)

													if (instrumentIdIndex == -1) {
														$scope.instrumentCount
																.push(1);
														$scope.labels_instrument
																.push(data.instrumentId);
													} else {
														$scope.instrumentCount
																.splice(
																		instrumentIdIndex,
																		1,
																		$scope.instrumentCount[instrumentIdIndex] + 1);
														$scope.labels_instrument
																.splice(
																		instrumentIdIndex,
																		1,
																		data.instrumentId)
													}

													// increment the msg count
													$scope.totalMsgCount
															.splice(
																	0,
																	1,
																	$scope.totalMsgCount[0] + 1);

												}

											});

							$scope.close = function(result) {
								close(result, 1500);
							}
						} ])
		.controller(
				'ModalCreateNewController',
				[
						'$scope',
						'$element',
						'$http',
						'injectId',
						'close',
						'TradeInjectorService',
						'filterFilter',
						function($scope, $element, $http, injectId, close,
								TradeInjectorService, filterFilter) {

							$scope.showStatus = [];
							// $scope.tradeInjectorProfile={};

							$scope.add = function() {
								alert('we are in add');
							};

							$scope.addInstruments = function() {
								alert('we are in add Instruments');
							};

							$scope.save = function(isValid) {

								// check to make sure the form is completely
								// valid
								if (isValid) {

									$scope.showStatus = [];
									var data = $scope.tradeInjectorProfile;
									console.log('Data before post '
											+ $scope.tradeInjectorProfile);

									var config = {
										headers : {
											'Content-Type' : 'application/json;'
										}
									}

									$http
											.post('/saveTradeInjectProfile',
													data, config)
											.success(
													function(data, status,
															headers, config) {
														$scope.PostDataResponse = data;
														$scope.showStatus = "Success!";
														console
																.log('Data received after save '
																		+ data);

													})
											.error(
													function(data, status,
															header, config) {
														$scope.showStatus = "Data: "
																+ data
																+ "<hr />status: "
																+ status
																+ "<hr />headers: "
																+ header
																+ "<hr />config: "
																+ config;
													});

								}

							};

							$scope.close = function(result) {
								close(result, 500);
							};
						} ]).controller(
				"TradeInjectProfileTableDisplay",
				function($scope, $http, $location, TradeInjectorService,
						ModalService) {

					$scope.tradeInjectProfiles = [];
					$scope.showStatus = [];

					$http.get("/getAllInjectProfiles").success(function(data) {

						$scope.tradeInjectProfiles = data;
					}).error(function(data) {
						$scope.showStatus = data;
					});

					// refresh the entire table
					$scope.refreshAll = function() {

						$http.get("/getAllInjectProfiles").success(
								function(data) {

									$scope.tradeInjectProfiles = data;
								}).error(function(data) {
							$scope.showStatus = data;
						});

					};

				});
