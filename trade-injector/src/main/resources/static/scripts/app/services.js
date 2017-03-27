angular.module("TradeInjectorApp.services").service("TradeInjectorService", function($q, $timeout){
	// Register the listeners in here
	
	var service= {},
	listenerTrades = $q.defer(),
	socket={
		client:null,
		stomp:null
	},
	messageIds=[];
	
	service.RECONNECT_TIMEOUT=30000;
	service.SOCKET_URL="/tradeInjector/injectorUI";
	service.TRADE_TOPIC="/topic/tradeAck";
	service.TRADE_BROKER="/app/tradeInjector";
	
	service.receive = function(){
		return listenerTrades.promise;
	};
	
	service.send=function(injectMessage){
		var id = Math.floor(Math.random() * 1000000);
		socket.stomp.send(service.TRADE_BROKER, 
				{priority: 9},
				JSON.stringify({
					injectMessage: injectMessage,
					id: id
				})
		);
		messageIds.push(id);
	};
	
	var reconnect = function(){
		$timeout(function(){
			initialize();
		}, 
		this.RECONNECT_TIMEOUT				
		);
	};
	
	var getTradeAcks = function(acks){
		var trades = JSON.parse(acks)
		return trades;
	};
	
	var startListener = function(){
		socket.stomp.subscribe(service.TRADE_TOPIC, function(data){
			listenerTrades.notify(getTradeAcks(data.body));
		});
	};
	
	var initialize = function(){
		socket.client = new SockJS(service.SOCKET_URL);
		socket.stomp = Stomp.over(socket.client);
		socket.stomp.connect({}, startListener);
		socket.stomp.onclose = reconnect;
	};
	
	initialize();
	return service;
});