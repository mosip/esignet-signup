// Get the current URL
const currentUrl = window.location.href;

// Create a URL object
const url = new URL(currentUrl);

// Get the search parameters (query params)
const searchParams = new URLSearchParams(url.search);

// Access specific query parameters
const slotId = searchParams.get('slotId');

const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8089/v1/signup/ws?slotId='+slotId
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/'+slotId, (resp) => {
        showGreeting(resp.body);
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

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
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    console.log('Sending message to server ', $("#step-code").val(),  $("#frame-order").val());
    stompClient.publish({
        destination: "/v1/signup/ws/process-frame",
        body: JSON.stringify({'slotId': slotId, 'stepCode':  $("#step-code").val(), "frames": [{"frame": "", "order":  $("#frame-order").val()}]})
    });
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#send" ).click(() => sendName());
});