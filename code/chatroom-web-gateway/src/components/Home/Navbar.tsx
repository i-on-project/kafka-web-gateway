"use client";
import React, {useState} from "react";
import Image from "next/image";
import {AiOutlineClose, AiOutlineMenu} from "react-icons/ai";

function Navbar() {

    const [navbarActive, setNavbarActive] = useState(false);

    return (
        <div className="flex fixed justify-between items-center px-5 lg:px-36 w-screen h-[100px] bg-white">
            <Image src="/images/logo.png" alt="logo" height={60} width={80}/>
            <div
                className="flex gap-10 font-medium lg:hidden"
                onClick={() => setNavbarActive((prev) => !prev)}
            >
                {navbarActive ? (
                    <AiOutlineClose size={30}/>
                ) : (
                    <AiOutlineMenu size={30}/>
                )}
            </div>
        </div>
    );
}

export default Navbar;
