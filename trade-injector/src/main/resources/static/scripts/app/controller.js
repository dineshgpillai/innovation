angular.module("TradeInjectorApp.controllers").controller(
		"TradeInjectCtrl",
		function($scope, TradeInjectorService) {

			$scope.tradeAcks = [];
			// $scope.tradeInjectorMessage = [];
			$scope.labels = [];
			$scope.data = [];
			$scope.tradeCount = [];

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

			$scope.injectTrades = function() {
				$scope.tradeAcks = [];
				$scope.labels = [];
				$scope.data = [];
				$scope.tradeCount = [];
				console.log('Before sending ' + $scope.tradeInjectorMessage);
				TradeInjectorService.send($scope.tradeInjectorMessage);
			};

			// Receives the trade ack and summarises the trade count
			TradeInjectorService.receive().then(
					null,
					null,
					function(data) {
						$scope.tradeAcks.push(data);
						var clientNameIndex = $scope.labels
								.indexOf(data.clientName)

						if (clientNameIndex == -1) {
							// this is a new client set the trade count
							// appropriately
							$scope.tradeCount.push(1);
							$scope.labels.push(data.clientName);
						} else {
							// existing client, update the trade count
							$scope.tradeCount.splice(clientNameIndex, 1,
									$scope.tradeCount[clientNameIndex] + 1);
							$scope.labels.splice(clientNameIndex, 1,
									data.clientName);
						}
					});

		}

);