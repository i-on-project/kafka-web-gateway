import IRoom from "./IRoom";
import React from "react";

export default interface IRoomContext {
    rooms: IRoom[];
    myRooms: IRoom[];
    setMyRooms: React.Dispatch<IRoom[]>;
}
