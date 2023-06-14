// Initialize the WebSocketPubSub instance
class GatewayClient {
    #url;
    #socket;
    #subscriptions;
    #onOpenCallback;
    #onCloseCallback;
    #active;

    constructor(url) {
        this.#url = url;
        this.#socket = null;
        this.#subscriptions = new Map();
        this.#onOpenCallback = null;
        this.#onCloseCallback = null;
    }

    connect(token) {
        this.#socket = new WebSocket(`${this.#url}?token=${token}`);

        this.#socket.onopen = () => {
            console.log('WebSocket connection established.');
            if (typeof this.#onOpenCallback === 'function') {
                this.#active = true
                this.#onOpenCallback();
            }
        };

        this.#socket.onmessage = (event) => {
            const message = JSON.parse(event.data);
            this.#handleMessage(message);
        };

        this.#socket.onclose = () => {
            console.log('WebSocket connection closed.');
            if (typeof this.#onCloseCallback === 'function') {
                this.#active = false;
                this.#onCloseCallback();
            }
        };

        return this;
    }

    disconnect() {
        if (this.#socket) {
            this.#socket.close();
            this.#socket = null;
            this.#subscriptions.clear();
            console.log('WebSocket connection disconnected.');
        }
    }

    onOpen(callback) {
        this.#onOpenCallback = callback;
    }

    onClose(callback) {
        this.#onCloseCallback = callback;
    }

    subscribe(topic, key, callback) {
        const subscriptionKey = this.#getSubscriptionKey(topic, key);
        if (!this.#subscriptions.has(subscriptionKey)) {
            this.#subscriptions.set(subscriptionKey, {
                topic,
                key,
                callback,
                lastMessageId: null,
                ackSent: false,
            });
        }
    }

    publish(topic, key, message) {
        const payload = {
            command: {
                type: 'publish',
                topic: topic,
                key: key,
                value: message
            }
        };
        this.#send(payload);
    }

    #handleMessage(message) {
        if (message.command.type === 'ack') {
            console.debug(`Ack received for ${message.messageId}`)
        } else if (message.command.type === 'message') {
            const topic = message.command.topic;
            const key = message.command.key;
            const subscriptionKey = this.#getSubscriptionKey(topic, key);
            const subscription = this.#subscriptions.get(subscriptionKey);
            if (subscription && message.messageId !== subscription.lastMessageId) {
                subscription.callback(message);
                subscription.lastMessageId = message.messageId;
                this.#sendAck(message.messageId);
            }
        } else {
            console.warn("Unknown message received")
        }
    }

    #send(payload) {
        if (this.#socket && this.#socket.readyState === WebSocket.OPEN) {
            const message = JSON.stringify(payload);
            this.#socket.send(message);
        }
    }

    #sendAck(messageId) {
        const payload = {
            command: {
                type: 'ack'
            },
            id: messageId,
        };
        this.#send(payload);
    }

    #getSubscriptionKey(topic, key) {
        return `${topic}-${key}`;
    }
}

const pubSub = new GatewayClient('ws://localhost:8080/socket');

// DOM elements
const loginContainer = document.getElementById('login-container');
const usernameInput = document.getElementById('username-input');
const loginButton = document.getElementById('login-button');
const chatContainer = document.getElementById('chat-container');
const topicList = document.getElementById('topic-list');
const messageContainer = document.getElementById('message-container');
const messageInput = document.getElementById('message-input');
const sendButton = document.getElementById('send-button');

// Event listener for login button click
loginButton.addEventListener('click', () => {
    const username = usernameInput.value.trim();
    if (username !== '') {
        // Hide the login container and show the chat container
        loginContainer.style.display = 'none';
        chatContainer.style.display = 'block';

        // Connect to the WebSocket server with the username as the token
        pubSub.connect(username);
    }
});

// Callback function to handle received messages
const handleMessage = (message) => {
    const {topic, key, value} = message;
    const messageText = `[${topic}] ${value}`;

    // Display the received message in the chat interface
    const messageElement = document.createElement('div');
    messageElement.textContent = messageText;
    messageContainer.appendChild(messageElement);
};

// Event listener for send button click
sendButton.addEventListener('click', () => {
    const message = messageInput.value;
    if (message.trim() !== '') {
        const selectedTopic = topicList.value;
        const key = null;
        pubSub.publish(selectedTopic, key, message);
        messageInput.value = '';
    }
});

// Event listener for topic selection change
topicList.addEventListener('change', () => {
    const selectedTopic = topicList.value;
    messageContainer.innerHTML = '';
    pubSub.subscribe(selectedTopic, null, handleMessage);
});

// Function to update the list of topics based on user subscriptions
const updateTopicList = (rooms) => {
    topicList.innerHTML = '';

    rooms.forEach((room) => {
        const optionElement = document.createElement('option');
        optionElement.value = room.name;
        optionElement.textContent = room.name + ' (Allowed: ' + room.allowed.join(', ') + ')';
        topicList.appendChild(optionElement);
    });

    topicList.dispatchEvent(new Event('change'));
};

// Callback function to handle user subscriptions
const handleSubscriptions = (subscriptions) => {
    // Update the list of topics based on user subscriptions
    updateTopicList(subscriptions);
};

async function fetchRooms() {
    try {
        const response = await fetch('http://localhost:8089/rooms');
        const data = await response.json();
        updateTopicList(data);
    } catch (error) {
        console.error('Error:', error);
    }
}

let fetchInterval;

// Callback function to handle the WebSocket connection status
const onOpen = () => {
    // Connection is established, fetch rooms
    fetchInterval = setInterval(fetchRooms, 5000);
};

const onClose = () => {
    // Connection is closed, clear the topic list and message container
    updateTopicList([]);
    messageContainer.innerHTML = '';
    clearInterval(fetchInterval);
};

// Set the callback functions for connection status and subscriptions
pubSub.onOpen(onOpen);
pubSub.onClose(onClose);
