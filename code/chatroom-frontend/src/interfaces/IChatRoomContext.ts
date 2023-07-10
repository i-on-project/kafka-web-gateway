import GatewayClient, {IGatewayMessage} from "@/lib/GatewayClient";
import React from "react";

export default interface IChatRoomContext {
    gateway: GatewayClient | undefined;
    roomUsers: { [roomId: string]: string[] };
    messages: { [roomId: string]: IGatewayMessage[] };
    addMessage: Function;
}
