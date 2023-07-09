import React from "react";
import {PropagateLoader} from "react-spinners";

function Status({text, isLoading}: {
    text: string;
    isLoading: boolean | undefined;
}) {

    return (
        <div className="flex justify-center items-center w-full text-xl">
            {isLoading ? (
                <PropagateLoader color="#9146FF" size={20}/>
            ) : (
                text
            )}
        </div>
    );
}

export default Status;