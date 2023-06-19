"use client";
import React, {useEffect, useRef} from "react";
import Avatar from "react-avatar";
import {useUser} from "@/contexts/UserContext";
import {IGatewayMessage} from "@/lib/GatewayClient";
import {useChatRoom} from "@/contexts/ChatRoomContext";

function ChatBody({roomId}: { roomId: string }) {
    const lastMessageRef = useRef<HTMLDivElement>(null);
    const {messages, gateway} = useChatRoom();
    const {username} = useUser();

    useEffect(() => {
        lastMessageRef.current?.scrollIntoView({behavior: "smooth"});
    }, [messages]);

    return (
        <div className="basis-[85%] overflow-y-scroll p-5 w-full flex flex-col gap-2">
            {messages[roomId]?.map((message: IGatewayMessage, index: number) =>
                message.command.key === username ? (
                    <div className="flex self-end" key={index}>
                        <div
                            className="flex justify-center items-center px-3 py-1 text-white rounded-full rounded-br-none bg-primary">
                            <p className="font-sans">{message.command.value}</p>
                        </div>
                    </div>
                ) : (
                    <div className="flex gap-2 self-start" key={index}>
                        <div className="self-end">
                            <Avatar
                                name={message.command.key}
                                round={true}
                                size="30"
                                className="text-sm"
                            />
                        </div>
                        <div>
                            <p className="pl-2 text-xs">{message.command.key}</p>
                            <div
                                className="flex justify-center items-center px-3 py-1 bg-gray-200 rounded-full rounded-tl-none">
                                <p className="font-sans">{message.command.value}</p>
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
