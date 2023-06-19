"use client";

import IUserContext from "@/interfaces/IUserContext";
import React, {createContext, useContext, useState} from "react";

const initialData: IUserContext = {
    username: "",
    setUsername: () => {
    },
};

const UserContext = createContext<IUserContext>(initialData);

export function useUser() {
    return useContext(UserContext);
}

export default function UserProvider({children}: { children: React.ReactNode; }) {

    const [username, setUsername] = useState<string>("");

    return (
        <UserContext.Provider
            value={{
                username,
                setUsername,
            }}
        >
            {children}
        </UserContext.Provider>
    );
}
