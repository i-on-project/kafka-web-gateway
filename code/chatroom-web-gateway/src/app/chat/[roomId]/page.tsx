"use client";
import ChatBody from "@/components/Chat/ChatBody";
import ChatFooter from "@/components/Chat/ChatFooter";
import ChatHeader from "@/components/Chat/ChatHeader";
import {useUser} from "@/contexts/UserContext";
import {useParams} from "next/navigation";
import React, {useEffect} from "react";
import {useChatRoom} from "@/contexts/ChatRoomContext";

function Page() {
    const {roomId} = useParams();
    const {gateway, roomUsers, addMessage} = useChatRoom();
    const {username} = useUser();

    useEffect(() => {
        if (roomUsers[roomId]?.includes(username)) return;
        gateway?.subscribe(
            roomId,
            undefined,
            (message: any) => {
                if (message.command.type === 'message') {
                    addMessage(roomId, message);
                } else {
                    console.log(`Unknown message received ${message}`)
                }
            },
            (resultMessage: any) => {
                if (resultMessage.command.type === 'ack') { // TODO: Implement server side verification of this message.
                    gateway?.publish(roomId, username, JSON.stringify({type: "join"}), undefined)
                } else if (resultMessage.command.type === 'error') {
                    alert(`Failed to subscribe: ${resultMessage.command.error}`);
                } else {
                    console.log(`Unknown message received ${resultMessage}`)
                }
            });
    }, []);

    return (
        <div className="flex relative flex-col w-full h-screen">
            <ChatHeader roomId={roomId}/>
            <ChatBody roomId={roomId}/>
            <ChatFooter roomId={roomId}/>
        </div>
    );
}

export default Page;
