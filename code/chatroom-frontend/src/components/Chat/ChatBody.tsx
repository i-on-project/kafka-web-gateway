"use client";
import React, {useEffect, useRef} from "react";
import Avatar from "react-avatar";
import {useUser} from "@/contexts/UserContext";
import {IGatewayMessage} from "@/lib/GatewayClient";
import {useChatRoom} from "@/contexts/ChatRoomContext";

interface MessageBody {
    key: string,
    type: string,
    text: string | undefined
}

function ChatBody({roomId}: { roomId: string }) {
    const lastMessageRef = useRef<HTMLDivElement>(null);
    const {messages, gateway} = useChatRoom();
    const {username} = useUser();

    useEffect(() => {
        lastMessageRef.current?.scrollIntoView({behavior: "smooth"});
    }, [messages]);

    const messagesBody: MessageBody[] = messages[roomId]?.map((message: IGatewayMessage, index: number) => {
        const value = JSON.parse(message.command.value)
        return {
            key: message.command.key,
            type: value.type,
            text: value.text,
        } as MessageBody
    })

    return (
        <div className="basis-[85%] overflow-y-scroll p-5 w-full flex flex-col gap-2">
            {messagesBody?.map((message, index) =>
                message.type === 'join' ? (
                        <div className="flex self-center" key={index}>
                            <div className="flex justify-center items-center">
                                <p>{message.key} joined the room.</p>
                            </div>
                        </div>
                    ) :
                    message.key === username ? (
                        <div className="flex self-end" key={index}>
                            <div
                                className="flex justify-center items-center px-3 py-1 text-white rounded-full rounded-br-none bg-primary">
                                <p className="font-sans">{message.text}</p>
                            </div>
                        </div>
                    ) : (
                        <div className="flex gap-2 self-start" key={index}>
                            <div className="self-end">
                                <Avatar
                                    name={message.key}
                                    round={true}
                                    size="30"
                                    className="text-sm"
                                />
                            </div>
                            <div>
                                <p className="pl-2 text-xs">{message.key}</p>
                                <div
                                    className="flex justify-center items-center px-3 py-1 bg-gray-200 rounded-full rounded-tl-none">
                                    <p className="font-sans">{message.text}</p>
                                </div>
                            </div>
                        </div>
                    )
            )}
            <div ref={lastMessageRef}/>
        </div>
    );
}

export default ChatBody;
