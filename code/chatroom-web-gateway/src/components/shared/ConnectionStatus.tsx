"use client"
import React, {useEffect, useState} from "react";
import {useChatRoom} from "@/contexts/ChatRoomContext";
import {useUser} from "@/contexts/UserContext";

export function ConnectionStatus() {
    const {gateway} = useChatRoom();
    const {username} = useUser();
    const [isOnline, setIsOnline] = useState<boolean>(false);

    useEffect(() => {
        const handleOpen = () => {
            setIsOnline(true);
        };

        const handleClose = () => {
            setIsOnline(false);
        };

        if (gateway) {
            gateway.onOpen(handleOpen);
            gateway.onClose(handleClose);
        }

        return () => {
            if (gateway) {
                gateway.onOpen(() => {
                });
                gateway.onClose(() => {
                });
            }
        };
    }, [gateway]);

    const handleReconnect = () => {
        if (gateway) {
            gateway.connect(username)
        }
    };

    return (
        <div className="flex justify-center items-center">
            {isOnline ? (
                <div className="px-2 pt-3 text-lg font-semibold sm:text-xl sm:px-5 bg-green-500 text-white p-2">
                    Connected
                </div>
            ) : (
                <div
                    className="flex justify-center items-center px-2 pt-3 text-lg font-semibold sm:text-xl sm:px-5 bg-red-500 text-white p-2">
                    Not Connected
                    <button
                        className="flex justify-center items-center ml-2 px-4 py-2 bg-white text-red-500 rounded-md"
                        onClick={handleReconnect}
                    >
                        Reconnect
                    </button>
                </div>
            )}
        </div>
    );
}

export default ConnectionStatus;
