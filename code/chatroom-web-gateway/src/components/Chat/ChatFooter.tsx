import {useUser} from "@/contexts/UserContext";
import React, {useState} from "react";
import {AiFillPlusCircle} from "react-icons/ai";
import {BsImage} from "react-icons/bs";
import {IoMdSend} from "react-icons/io";
import {useChatRoom} from "@/contexts/ChatRoomContext";

function ChatFooter({roomId}: { roomId: string }) {

    const [message, setMessage] = useState<string>("");
    const {gateway} = useChatRoom();
    const {username} = useUser();

    const handleSendMessage = (e: any, message: string) => {
        e.preventDefault();
        if (message.trim()) {
            gateway?.publish(roomId, username, message);
        }
        setMessage("");
    };

    return (
        <div className="basis-[8%] border-t-2 p-2 flex items-center gap-4">
            {message.length === 0 && (
                <>
                    <AiFillPlusCircle size={20} className="cursor-pointer text-primary"/>
                    <BsImage size={20} className="cursor-pointer text-primary"/>
                </>
            )}
            <div className="relative w-full">
                <form onSubmit={(e) => handleSendMessage(e, message)}>
                    <input
                        type="text"
                        value={message}
                        className="p-2 w-full h-8 bg-gray-100 rounded-full transition-all focus:outline-none"
                        placeholder="Aa"
                        onChange={(e) => setMessage(e.target.value)}
                    />
                </form>
            </div>
            {message.length === 0 ? (
                <IoMdSend
                    size={28}
                    className="cursor-pointer text-primary"
                    aria-disabled={true}
                />
            ) : (
                <IoMdSend
                    size={28}
                    className="cursor-pointer text-primary"
                    onClick={(e) => handleSendMessage(e, message)}
                />
            )}
        </div>
    );
}

export default ChatFooter;
