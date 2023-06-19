"use client";
import IChatRoomContext from "@/interfaces/IChatRoomContext";
import React, {createContext, useContext, useEffect, useState} from "react";
import {useUser} from "./UserContext";
import {useRouter} from "next/navigation";
import GatewayClient, {IGatewayMessage} from "@/lib/GatewayClient";

const initialData: IChatRoomContext = {
    gateway: undefined,
    roomUsers: {},
    messages: {},
    addMessage: () => {
    },
};

const ChatRoomContext = createContext<IChatRoomContext>(initialData);

export function useChatRoom() {
    return useContext(ChatRoomContext);
}

export default function ChatRoomProvider({children}: { children: React.ReactNode; }) {

    const [roomUsers, setRoomUsers] = useState<{ [roomId: string]: string[] }>({});
    const [gateway, setGateway] = useState<GatewayClient | undefined>();
    const [messages, setMessages] = useState<{ [roomId: string]: IGatewayMessage[] }>({});

    const addMessage = (roomId: string, message: IGatewayMessage) => {
        setMessages((prevMessages) => ({
            ...prevMessages,
            [roomId]: [...(prevMessages[roomId] ?? []), message],
        }));
    };

    const {username} = useUser();
    const router = useRouter();

    useEffect(() => {
        if (!username) {
            router.replace("/");
            return;
        }
        const gateway = new GatewayClient('ws://localhost:8083/socket').connect(username); // TODO: set to 8080
        setGateway(gateway);
    }, []);

    return (
        <ChatRoomContext.Provider value={{gateway, roomUsers, messages, addMessage: addMessage}}>
            {children}
        </ChatRoomContext.Provider>
    );
}
