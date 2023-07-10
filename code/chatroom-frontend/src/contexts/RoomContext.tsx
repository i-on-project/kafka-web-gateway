"use client";
import IRoom from "@/interfaces/IRoom";
import IRoomContext from "@/interfaces/IRoomContext";
import React, {createContext, useContext, useEffect, useState} from "react";

const initialData: IRoomContext = {
    rooms: [],
    myRooms: [],
    setMyRooms: () => {
    },
};

const RoomContext = createContext<IRoomContext>(initialData);

export function useRoom() {
    return useContext(RoomContext);
}

export default function RoomProvider({children}: { children: React.ReactNode; }) {

    const [rooms, setRooms] = useState<IRoom[]>([]);
    const [myRooms, setMyRooms] = useState<IRoom[]>([]);

    useEffect(() => {
        fetchRoomsFromServer();
        fetchMyRooms();
    }, []);

    useEffect(() => {
        updateMyRooms();
    }, [myRooms]);

    async function fetchRoomsFromServer(): Promise<void> {
        const response = await fetch("http://localhost:8089/rooms");
        const rooms = await response.json();
        setRooms(rooms);
    }

    function fetchMyRooms() {
        const myRooms = localStorage.getItem("myRooms");
        if (myRooms) setMyRooms(JSON.parse(myRooms));
        else setMyRooms([]);
    }

    function updateMyRooms() {
        localStorage.setItem("myRooms", JSON.stringify(myRooms));
    }

    return (
        <RoomContext.Provider value={{rooms, myRooms, setMyRooms}}>
            {children}
        </RoomContext.Provider>
    );
}
