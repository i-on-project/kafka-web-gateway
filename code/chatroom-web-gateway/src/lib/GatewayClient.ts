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
    messageCallback: Function | undefined;
    lastMessageId: string | undefined;
    ackSent: boolean;
}

export default class GatewayClient {
    readonly #url: string;
    #socket: WebSocket | undefined;
    #fullTopicsSubscriptions: Map<string, ISubscription>;
    #keysSubscriptions: Map<string, ISubscription>;
    #operationsCallback: Map<string, { expiresAt: number, operationCallback: Function | undefined }>; // TODO: Implement scheduled task to periodically clean callbacks that might have gotten lost(by comparing timestamps).
    #onOpenCallback: Function | undefined; // TODO: Also for the operationCallback, maybe implement a setTimeout task that checks if it received a message from the server, otherwise execute callback with "err" message
    #onCloseCallback: Function | undefined;
    #active: boolean;

    constructor(url: string) {
        this.#url = url;
        this.#socket = undefined;
        this.#fullTopicsSubscriptions = new Map();
        this.#keysSubscriptions = new Map();
        this.#operationsCallback = new Map();
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
            this.#handleMessage(JSON.parse(event.data));
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

    subscribe(topic: string, key: string | undefined, messageCallback: Function | undefined, operationCallback: Function | undefined) {
        const messageId = crypto.randomUUID()

        if (!this.#operationsCallback.has(messageId)) {
            this.#operationsCallback.set(messageId, {
                expiresAt: Date.now() + 60_000,
                operationCallback: operationCallback
            });
        }

        const subscriptionKey = this.#getSubscriptionKey(topic, key);
        if (key == null) {
            if (!this.#fullTopicsSubscriptions.has(topic)) {
                this.#fullTopicsSubscriptions.set(topic, {
                    topic,
                    key: undefined,
                    messageCallback: messageCallback,
                    lastMessageId: undefined,
                    ackSent: false,
                });
            }
        } else if (!this.#keysSubscriptions.has(subscriptionKey)) {
            this.#keysSubscriptions.set(subscriptionKey, {
                topic,
                key: undefined,
                messageCallback: messageCallback,
                lastMessageId: undefined,
                ackSent: false,
            });
        }

        // Send the subscribe command to the server
        const payload = {
            messageId,
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

    publish(topic: string, key: string | undefined, message: string, operationCallback: Function | undefined) {
        const messageId = crypto.randomUUID()

        if (!this.#operationsCallback.has(messageId)) {
            this.#operationsCallback.set(messageId, {
                expiresAt: Date.now() + 60_000,
                operationCallback: operationCallback
            });
        }

        const payload = {
            messageId,
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

        if (message.command.type === 'ack' || message.command.type === 'error') {
            console.info(`Ack || error received for ${message.messageId}`)

            let operationsCallback = this.#operationsCallback.get(message.messageId);

            if (operationsCallback) {
                if (operationsCallback.operationCallback) {
                    operationsCallback.operationCallback(message)
                }
                this.#operationsCallback.delete(message.messageId)
            }

        } else if (message.command.type === 'message') {

            const subscriptionKey = this.#getSubscriptionKey(message.command.topic, message.command.key);
            let subscription;

            if (this.#fullTopicsSubscriptions.has(message.command.topic)) {
                subscription = this.#fullTopicsSubscriptions.get(message.command.topic)
            } else {
                subscription = this.#keysSubscriptions.get(subscriptionKey);
            }

            // if it has subscription, execute callback
            if (subscription && message.messageId !== subscription.lastMessageId) {
                if (subscription.messageCallback) {
                    subscription.messageCallback(message)
                }
                subscription.lastMessageId = message.messageId;
            }

            this.#sendAck(message.messageId);
        } else {
            console.error("Unknown message received, check gateway for root cause.")
            console.log(message)
        }
    }

    #send(payload: any) {
        if (this.#socket && this.#socket.readyState === WebSocket.OPEN) {
            const message = JSON.stringify(payload);
            console.log('Sent: ')
            console.log(payload)
            this.#socket.send(message);
        }
    }

    #sendAck(messageId: string) {
        const payload = {
            messageId,
            command: {
                type: 'ack'
            },
        };
        this.#send(payload);
    }

    #getSubscriptionKey(topic: string, key: string | undefined) {
        return `${topic}-${key}`;
    }
}