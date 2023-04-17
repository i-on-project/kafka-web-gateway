let ws;

function setConnected(connected) {
    document.getElementById("connect").disabled = connected;
    document.getElementById("disconnect").disabled = !connected;
    document.getElementById("send").disabled = !connected;
    if (connected) {
        document.getElementById("conversation").style.display = "block";
    } else {
        document.getElementById("conversation").style.display = "none";
    }
    document.getElementById("greetings").innerHTML = "";
}

function connect() {
    ws = new WebSocket(document.getElementById("url").value);
    ws.onopen = function () {
        setConnected(true);
    }
    ws.onmessage = function (data) {
        showGreeting(data.data);
    };
    ws.onclose = function () {
        setConnected(false);
    }
}

function disconnect() {
    if (ws != null) {
        ws.close();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    const data = JSON.stringify({'name': document.getElementById("name").value});
    ws.send(data);
}

function showGreeting(message) {
    const greetings = document.getElementById("greetings");
    const tr = document.createElement("tr");
    const td = document.createElement("td");
    td.innerHTML = message;
    tr.appendChild(td);
    greetings.appendChild(tr);
}

window.addEventListener('DOMContentLoaded', function () {
    document.getElementById("conversation").style.display = "none";
    document.getElementById("send").disabled = true;
    document.querySelector("form").addEventListener('submit', function (e) {
        e.preventDefault();
    });
    document.getElementById("connect").addEventListener('click', function () {
        connect();
    });
    document.getElementById("disconnect").addEventListener('click', function () {
        disconnect();
    });
    document.getElementById("send").addEventListener('click', function () {
        sendName();
    });
});

