let outputElement
let subscribeTopicsInput
let consumeQuantityInput
let consumeScaleInput
let publishTopicInput
let publishKeyInput
let publishValueInput
let pauseTopicsInput
let resumeTopicsInput

document.addEventListener('DOMContentLoaded', function () {
    outputElement = document.getElementById('output');
    subscribeTopicsInput = document.getElementById('subscribeTopics');
    consumeQuantityInput = document.getElementById('consumeQuantity');
    consumeScaleInput = document.getElementById('consumeScale');
    publishTopicInput = document.getElementById('publishTopic');
    publishKeyInput = document.getElementById('publishKey');
    publishValueInput = document.getElementById('publishValue');
    pauseTopicsInput = document.getElementById('pauseTopics');
    resumeTopicsInput = document.getElementById('resumeTopics');
});

const webSocket = new WebSocket('ws://localhost:8080/socket?token=teste');

webSocket.onopen = function () {
    console.log('WebSocket connection established.');
};

webSocket.onmessage = function (event) {
    const message = JSON.parse(event.data);
    console.log('Received message:', message);

    const listItem = document.createElement('li');
    listItem.classList.add('list-group-item');
    listItem.textContent = JSON.stringify(message);
    outputElement.appendChild(listItem);
};

function subscribe() {
    const topics = subscribeTopicsInput.value.split(',').map(topic => topic.trim());
    const command = {
        messageID: generateMessageID(),
        command: {
            type: 'subscribe',
            topics: topics,
            startConsuming: false
        }
    };

    webSocket.send(JSON.stringify(command));
    console.log('Sent command:', command);
}

function consume() {
    const maxQuantity = parseInt(consumeQuantityInput.value);
    const scale = consumeScaleInput.value;
    const command = {
        messageID: generateMessageID(),
        command: {
            type: 'consume',
            maxQuantity: maxQuantity,
            scale: scale
        }
    };

    webSocket.send(JSON.stringify(command));
    console.log('Sent command:', command);
}

function publish() {
    const topic = publishTopicInput.value;
    const key = publishKeyInput.value;
    const value = publishValueInput.value;
    const command = {
        messageID: generateMessageID(),
        command: {
            type: 'publish',
            topic: topic,
            key: key,
            value: value
        }
    };

    webSocket.send(JSON.stringify(command));
    console.log('Sent command:', command);
}

function pause() {
    const topics = pauseTopicsInput.value.split(',').map(topic => topic.trim());
    const command = {
        messageID: generateMessageID(),
        command: {
            type: 'pause',
            topics: topics
        }
    };

    webSocket.send(JSON.stringify(command));
    console.log('Sent command:', command);
}

function resume() {
    const topics = resumeTopicsInput.value.split(',').map(topic => topic.trim());
    const command = {
        messageID: generateMessageID(),
        command: {
            type: 'resume',
            topics: topics
        }
    };

    webSocket.send(JSON.stringify(command));
    console.log('Sent command:', command);
}

function generateMessageID() {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}