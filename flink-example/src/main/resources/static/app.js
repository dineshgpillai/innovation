var app = angular.module('Hello', ['doowb.angular-pusher']); 

app.config(['PusherServiceProvider',
        function(PusherServiceProvider) {
          PusherServiceProvider
          .setToken('your_app_key')
          .setOptions({});
        }
 ]);

app.controller('myCtrl', ['$scope', '$http', 'Pusher', function($scope, $http, Pusher) {
	    $scope.firstName= "John";
	    $scope.lastName= "Doe";
		$scope.counter= "Dummy";
		
		//var client = new Pusher(API_KEY);
		//var pusher = $pusher(client);
		//var my_channel = pusher.subscribe('/topic/greetings');
		//my_channel.bind('firstName', function(data) {
		// with greeting
			
		//}
		//);	
		
		
	
}]); 


var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});