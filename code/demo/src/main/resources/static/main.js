// Initialize the GatewayClient instance
class GatewayClient {
    #url;
    #socket;
    #fullTopicsSubscriptions;
    #keysSubscriptions;
    #onOpenCallback;
    #onCloseCallback;
    #active;

    constructor(url) {
        this.#url = url;
        this.#socket = null;
        this.#fullTopicsSubscriptions = new Map();
        this.#keysSubscriptions = new Map();
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
            this.#keysSubscriptions.clear();
            this.#fullTopicsSubscriptions.clear()
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
        if (key == null) {
            if (!this.#fullTopicsSubscriptions.has(topic)) {
                this.#fullTopicsSubscriptions.set(topic, {
                    topic,
                    key: null,
                    callback,
                    lastMessageId: null,
                    ackSent: false,
                });
            }
        } else {
            const subscriptionKey = this.#getSubscriptionKey(topic, key);

            if (!this.#keysSubscriptions.has(topic)) {
                this.#keysSubscriptions.set(subscriptionKey, {
                    topic,
                    key: null,
                    callback,
                    lastMessageId: null,
                    ackSent: false,
                });
            }
        }

        // Send the subscribe command to the server
        const payload = {
            command: {
                type: 'subscribe',
                topics: [{
                    topic: topic,
                    key: key
                }]
            }
        };
        this.#send(payload);
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
            console.info(`Ack received for ${message.messageId}`)
        } else if (message.command.type === 'message') {
            console.info(`Not ack received ${message}`)
            const topic = message.command.topic;
            const key = message.command.key;

            let subscription;
            if (this.#fullTopicsSubscriptions.has(topic)) {
                subscription = this.#fullTopicsSubscriptions.get(topic)
            } else {
                const subscriptionKey = this.#getSubscriptionKey(topic, key);
                subscription = this.#keysSubscriptions.get(subscriptionKey);
            }

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
            console.log('Send with ')
            console.log(payload)
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

    // Criar subscrição com topic = "a" e key = null
    // Obter subscrição, com topic = "a" e key = (chave do recorde(!= null))
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
const roomList = document.getElementById('room-list');

let clientId
// Event listener for login button click
loginButton.addEventListener('click', () => {
    const username = usernameInput.value.trim();
    if (username !== '') {
        // Hide the login container and show the chat container
        loginContainer.style.display = 'none';
        chatContainer.style.display = 'block';

        // Connect to the WebSocket server with the username as the token
        pubSub.connect(username);
        clientId = username
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

    // Subscribe to the selected topic
    pubSub.subscribe(selectedTopic, null, (message) => {
        console.log("message:");
        console.log(message);
        if (message.command.type === 'ack') {
            // Acknowledgement received
            console.log(`ACK CALLBACK`);
            displaySubscribedRoom(selectedTopic);

        } else if (message.command.type === 'err') {
            console.log(`ERR CALLBACK`);
            alert(`Failed to subscribe: ${message.command.error}`);
        } else if (message.command.type === 'message') {
            console.log(`MESSAGE CALLBACK`);
            // Handle regular message
            handleMessage(message);
        } else {
            console.log(`Unknown message received ${message}`)
        }
    });
});

// Function to update the list of rooms
const updateRoomList = (rooms) => {
    roomList.innerHTML = '';

    rooms.forEach((room) => {
        const roomElement = document.createElement('div');
        roomElement.value = room.name;
        roomElement.textContent = room.name + ' (Allowed: ' + room.allowed.join(', ') + ')';
        // Add click event listener to request permissions for the room
        roomElement.addEventListener('click', () => {
            requestPermissions(room.name);
        });

        roomList.appendChild(roomElement);
    });
};

// Function to update the list of topics based on user subscriptions
const updateTopicList = (topics) => {
    // Clear the topic list
    topicList.innerHTML = '';

    // Create an option for each topic and add it to the topic list
    topics.forEach((topic) => {
        const optionElement = document.createElement('option');
        optionElement.value = topic.name;
        optionElement.textContent = topic.name;
        topicList.appendChild(optionElement);
    });

    // Trigger the change event to subscribe to the first topic in the list
    topicList.dispatchEvent(new Event('change'));
};

// Callback function to handle user subscriptions
const handleAvailableRooms = (rooms) => {
    updateRoomList(rooms);
    updateTopicList(rooms);
};

// Display the currently subscribed room
const displaySubscribedRoom = (room) => {
    const roomElement = document.getElementById('subscribed-room');
    roomElement.textContent = `Subscribed to: ${room}`;
};

async function fetchRooms() {
    try {
        const response = await fetch('http://localhost:8089/rooms');
        const data = await response.json();
        handleAvailableRooms(data);
    } catch (error) {
        console.error('Error:', error);
    }
}

let fetchInterval;

// Callback function to handle the WebSocket connection status
const onOpen = () => {
    // Connection is established, fetch rooms
    // fetchInterval = setInterval(fetchRooms, 1500000);
};

// Function to request permissions for a room
async function requestPermissions(roomName) {
    try {
        const response = await fetch(`http://localhost:8089/room/${roomName}/${clientId}`);

        if (response.ok) {
            console.log(`Permissions requested for room: ${roomName}`);
        } else {
            console.error(`Failed to request permissions for room: ${roomName}`);
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

setTimeout(fetchRooms, 5000)


const onClose = () => {
    // Connection is closed, clear the topic list and message container
    updateRoomList([]);
    updateTopicList([]);
    messageContainer.innerHTML = '';
    clearInterval(fetchInterval);
};

// Set the callback functions for connection status and subscriptions
pubSub.onOpen(onOpen);
pubSub.onClose(onClose);
