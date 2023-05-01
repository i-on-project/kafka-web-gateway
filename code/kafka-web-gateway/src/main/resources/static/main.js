let ws;

function setConnectionState(connected) {
    document.getElementById("connect").disabled = connected;
    document.getElementById("disconnect").disabled = !connected;
    document.getElementById("subscribe").disabled = !connected;
    document.getElementById("produce").disabled = !connected;

    if (connected) {
        document.getElementById("conversation").style.display = "block";
    } else {
        document.getElementById("conversation").style.display = "none";
    }
    document.getElementById("records").innerHTML = "";
}

function connect() {
    ws = new WebSocket(document.getElementById("url").value);
    ws.onopen = function () {
        setConnectionState(true);
    }
    ws.onmessage = function (data) {
        showRecord(data.data);
    };
    ws.onclose = function () {
        setConnectionState(false);
    }
}

function disconnect() {
    if (ws != null) {
        ws.close();
    }
    setConnectionState(false);
    console.log("Disconnected");
}

function sendName() {
    const data = JSON.stringify({'name': document.getElementById("name").value});
    ws.send(data);
}

// TODO: change data sent to accept offset too
function sendSubscribe() {
    const topics = document.getElementById("subscribe-topic");
    const data = `subscribe${topics}`;
    ws.send(data);
}

function produceRecord() {
    const topic = document.getElementById("produce-topic");
    const key = document.getElementById("produce-key");
    const value = document.getElementById("produce-value");
    const data = `publish:${topic},${key},${value}`;
    ws.send(data);
}

function showRecord(message) {
    const greetings = document.getElementById("records");
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
    document.getElementById("subscribe").addEventListener('click', function () {
        sendSubscribe();
    });
    document.getElementById("produce").addEventListener('click', function () {
        produceRecord();
    });
});

