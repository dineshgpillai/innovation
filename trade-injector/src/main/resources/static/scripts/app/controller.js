angular.module("TradeInjectorApp.controllers").controller("TradeInjectCtrl",
		function($scope, TradeInjectorService) {

			$scope.tradeAcks = [];
			$scope.tradeInjectorMessage = [];
			$scope.labels=[];
			$scope.data=[];
			
			$scope.options={
					responsive: true,
					responsiveAnimationDuration: 1000,
					title:{
						display:true,
						text: 'Trade Count Chart'
					}
			};
			
			$scope.datasetOverride={fill: false};
			
			$scope.injectTrades = function(){
				$scope.tradeAcks = [];
				$scope.labels=[];
				$scope.data=[];
				
				TradeInjectorService.send($scope.tradeInjectorMessage);
			};
			
			TradeInjectorService.receive().then(null,null, function(data){
				$scope.tradeAcks.push(data);
			});
			
		}

);