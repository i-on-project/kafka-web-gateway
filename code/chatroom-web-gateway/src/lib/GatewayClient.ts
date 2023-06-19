export interface IGatewayMessage {
    messageId: string,
    command: {
        type: string,
        topic: string,
        partition: string,
        key: string | undefined,
        value: string,
        timestamp: string,
        offset: number
    }
}

export interface ISubscription {
    topic: string;
    key: string | undefined;
    callback: Function | undefined;
    lastMessageId: string | undefined;
    ackSent: boolean;
}

export default class GatewayClient {
    readonly #url: string;
    #socket: WebSocket | undefined;
    #fullTopicsSubscriptions: Map<string, ISubscription>;
    #keysSubscriptions: Map<string, ISubscription>;
    #onOpenCallback: Function | undefined;
    #onCloseCallback: Function | undefined;
    #active: boolean;

    constructor(url: string) {
        this.#url = url;
        this.#socket = undefined;
        this.#fullTopicsSubscriptions = new Map();
        this.#keysSubscriptions = new Map();
        this.#onOpenCallback = undefined;
        this.#onCloseCallback = undefined;
        this.#active = false;
    }

    connect(token: string) {
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
            this.#socket = undefined;
            this.#keysSubscriptions.clear();
            this.#fullTopicsSubscriptions.clear()
            console.log('WebSocket connection disconnected.');
        }
    }

    onOpen(callback: Function) {
        this.#onOpenCallback = callback;
    }

    onClose(callback: Function) {
        this.#onCloseCallback = callback;
    }

    subscribe(topic: string, key: string | undefined, callback: Function) {
        if (key == null) {
            if (!this.#fullTopicsSubscriptions.has(topic)) {
                this.#fullTopicsSubscriptions.set(topic, {
                    topic,
                    key: undefined,
                    callback,
                    lastMessageId: undefined,
                    ackSent: false,
                });
            }
        } else {
            const subscriptionKey = this.#getSubscriptionKey(topic, key);

            if (!this.#keysSubscriptions.has(topic)) {
                this.#keysSubscriptions.set(subscriptionKey, {
                    topic,
                    key: undefined,
                    callback,
                    lastMessageId: undefined,
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

    publish(topic: string, key: string | undefined, message: string) {
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

    #handleMessage(message: IGatewayMessage) {
        if (message.command.type === 'ack') {
            console.info(`Ack received for ${message.messageId}`)
        } else if (message.command.type === 'message') {
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
                if (subscription.callback !== undefined) {
                    console.log('found subscription')
                    subscription.callback(message)
                }
                subscription.lastMessageId = message.messageId;
                this.#sendAck(message.messageId);
            }
        } else {
            console.warn("Unknown message received")
            console.log(message)
        }
    }

    #send(payload: any) {
        if (this.#socket && this.#socket.readyState === WebSocket.OPEN) {
            const message = JSON.stringify(payload);
            console.log('Send with ')
            console.log(payload)
            this.#socket.send(message);
        }
    }

    #sendAck(messageId: string) {
        const payload = {
            command: {
                type: 'ack'
            },
            messageId: messageId,
        };
        this.#send(payload);
    }

    #getSubscriptionKey(topic: string, key: string | undefined) {
        return `${topic}-${key}`;
    }
}