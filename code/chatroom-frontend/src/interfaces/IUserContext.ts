import React from "react";

export default interface IUserContext {
    username: string;
    setUsername: React.Dispatch<string>;
}
