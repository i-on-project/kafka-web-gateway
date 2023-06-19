import RoomSideBar from "@/components/Room/RoomSideBar";
import RoomProvider from "@/contexts/RoomContext";
import ChatRoomProvider from "@/contexts/ChatRoomContext";
import React from "react";

export default function RoomLayout({children}: { children: React.ReactNode; }) {
    return (
        <RoomProvider>
            <ChatRoomProvider>
                <div className="flex h-screen">
                    <RoomSideBar/>
                    {children}
                </div>
            </ChatRoomProvider>
        </RoomProvider>
    );
}
