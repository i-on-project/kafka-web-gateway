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
            type: 'publish',
            topic,
            key,
            message,
        };
        this.#send(payload);
    }

    #handleMessage(message) {
        if (message.type === 'ack') {
            console.debug(`Ack received for ${message.id}`)
        } else if (message.type === 'message') {
            const topic = message.topic;
            const key = message.key;
            const subscriptionKey = this.#getSubscriptionKey(topic, key);
            const subscription = this.#subscriptions.get(subscriptionKey);
            if (subscription && message.id !== subscription.lastMessageId) {
                subscription.callback(message);
                subscription.lastMessageId = message.id;
                this.#sendAck(message.id);
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
            type: 'ack',
            id: messageId,
        };
        this.#send(payload);
    }

    #getSubscriptionKey(topic, key) {
        return `${topic}-${key}`;
    }
}
