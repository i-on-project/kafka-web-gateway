import {GatewayClient} from "./gateway-client";

// Connect to the Gateway server
const client = new GatewayClient('wss://example.com/ws').connect('token1');

// Define custom callbacks for onOpen and onClose events
client.onOpen(() => {
    console.log('WebSocket connection opened!');
});

client.onClose(() => {
    console.log('WebSocket connection closed!');
});

// Subscribe to a topic with a specific key and define a callback function
client.subscribe('topic1', 'key1', (message) => {
    console.log(`Received message on topic1 with key1: ${message.payload}`);
});

// Publish a message to a topic with a specific key
client.publish('topic1', 'key1', 'Hello, world!');

// Disconnect from the Gateway server
client.disconnect();
