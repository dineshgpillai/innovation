angular
		.module("TradeInjectorApp.controllers")
		.controller(
				"TradeInjectCtrl",
				function($scope, $http, TradeInjectorService) {

					$scope.tradeAcks = [];
					// $scope.tradeInjectorMessage = [];
					$scope.labels = [];
					$scope.data = [];
					$scope.tradeCount = [];
					$scope.showGeneration = false;
					$scope.totalMsgCount = [ 0 ];
					$scope.tab=1;
					
					$scope.setTab=function(newTab){
						$scope.tab=newTab;
					}
					
					$scope.isSet = function(tabNum){
						return $scope.tab === tabNum;
					}

					$scope.options = {
						responsive : true,
						responsiveAnimationDuration : 1000,
						title : {
							display : true,
							text : 'Trade Count Chart'
						}
					};

					$scope.datasetOverride = {
						fill : false
					};
					
					$scope.stop = function(){
						$http.post('/tradeInjector/tradeMessageStop');
						$scope.showGeneration = false;

					}

					$scope.injectTrades = function() {
						$scope.tradeAcks = [];
						$scope.labels = [];
						$scope.data = [];
						$scope.tradeCount = [];
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
											$scope.tradeCount.push(1);
											$scope.labels.push(data.clientName);
										} else {
											// existing client, update the trade
											// count
											$scope.tradeCount
													.splice(
															clientNameIndex,
															1,
															$scope.tradeCount[clientNameIndex] + 1);
											$scope.labels.splice(
													clientNameIndex, 1,
													data.clientName);
										}
										// increment the msg count
										$scope.totalMsgCount.splice(0, 1,
												$scope.totalMsgCount[0] + 1);

										
										//kill the generation message once we reach end
										if ($scope.totalMsgCount[0] == $scope.tradeInjectorMessage.noOfTrades) {
											$scope.showGeneration = false;
										}

									});

				}

		);