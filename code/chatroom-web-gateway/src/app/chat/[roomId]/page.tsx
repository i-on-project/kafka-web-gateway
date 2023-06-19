"use client";
import ChatBody from "@/components/Chat/ChatBody";
import ChatFooter from "@/components/Chat/ChatFooter";
import ChatHeader from "@/components/Chat/ChatHeader";
import {useUser} from "@/contexts/UserContext";
import {useParams} from "next/navigation";
import React, {useEffect} from "react";
import {useChatRoom} from "@/contexts/ChatRoomContext";
import {IGatewayMessage} from "@/lib/GatewayClient";

function Page() {
    const {roomId} = useParams();
    const {gateway, roomUsers, messages, addMessage} = useChatRoom();
    const {username} = useUser();

    const handleMessage = (message: IGatewayMessage) => {
        addMessage(roomId, message);
    }

    useEffect(() => {
        if (roomUsers[roomId]?.includes(username)) return;
        gateway?.subscribe(roomId, undefined, (message: any) => {
            console.log(message)
            if (message.command.type === 'message') {
                // Handle regular message
                handleMessage(message);
            } else if (message.command.type === 'ack') {
                // Acknowledgement received
                console.log("displaySubscribedRoom"); // TODO
                // displaySubscribedRoom(selectedTopic);

            } else if (message.command.type === 'err') {
                console.log(`ERR CALLBACK`);
                alert(`Failed to subscribe: ${message.command.error}`);
            } else {
                console.log(`Unknown message received ${message}`)
            }
        });
    }, [roomId, gateway]);

    return (
        <div className="flex relative flex-col w-full h-screen">
            <ChatHeader roomId={roomId}/>
            <ChatBody roomId={roomId}/>
            <ChatFooter roomId={roomId}/>
        </div>
    );
}

export default Page;
