angular
		.module("TradeInjectorApp.controllers")
		.controller(
				"TradeInjectCtrl",
				function($scope, $http, TradeInjectorService) {

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

					$scope.setTab = function(newTab) {
						$scope.tab = newTab;
					}

					$scope.isSet = function(tabNum) {
						return $scope.tab === tabNum;
					}

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

					$scope.stop = function() {
						$http.post('/tradeInjector/tradeMessageStop');
						$scope.showGeneration = false;

					}

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

										// kill the generation message once we
										// reach end
										if ($scope.totalMsgCount[0] == $scope.tradeInjectorMessage.noOfTrades) {
											$scope.showGeneration = false;
										}

									});

				}

		);