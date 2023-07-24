"use client";
import ChatBody from "@/components/Chat/ChatBody";
import ChatFooter from "@/components/Chat/ChatFooter";
import ChatHeader from "@/components/Chat/ChatHeader";
import {useUser} from "@/contexts/UserContext";
import {useParams} from "next/navigation";
import React, {useEffect, useState} from "react";
import {useChatRoom} from "@/contexts/ChatRoomContext";
import {IGatewayMessage} from "@/lib/GatewayClient";
import Status from "@/components/shared/Status";

function Page() {
    const {roomId} = useParams();
    const {gateway, roomUsers, addMessage} = useChatRoom();
    const {username} = useUser();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<any>(null);

    useEffect(() => {
        if (roomUsers[roomId]?.includes(username)){
            setLoading(false);
            return;
        }

        const joinRoom = async () => {
            try {
                const response = await fetch(`http://localhost:8089/room/${roomId}/${username}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${username}`
                    }
                });

                if (response.ok) {
                    roomUsers[roomId] = await response.json()
                    gateway
                        ?.subscribe(roomId, undefined, (message: IGatewayMessage) => {
                            if (message.command.type === "message") {
                                addMessage(roomId, message);
                            } else {
                                console.log(`Unknown message received ${message}`);
                            }
                        })
                        .then(() => {
                            setLoading(false);
                            roomUsers[roomId]?.push(username)
                            gateway?.publish(roomId, username, JSON.stringify({type: "join", text: ''}))
                        })
                        .catch((error) => {
                            setError(error);
                            setLoading(false);
                        });
                } else {
                    throw new Error('Failed to join the room.');
                }
            } catch (error) {
                setError(error);
                setLoading(false);
            }
        };

        joinRoom();
    }, []);

    if (loading) {
        return <Status text={''} isLoading={true}/>;
    }

    if (error) {
        return <Status text={`Error: ${error.message}`} isLoading={false}/>;
    }

    return (
        <div className="flex relative flex-col w-full h-screen">
            <ChatHeader roomId={roomId}/>
            <ChatBody roomId={roomId}/>
            <ChatFooter roomId={roomId}/>
        </div>
    );
}

export default Page;