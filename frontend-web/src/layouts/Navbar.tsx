import React from "react";
import logo from "../assets/images/logo.png";

function Navbar() {
    return (
        <header className="flex justify-between items-center p-4 bg-primary">
            <a href="/">
                <div className="text-lg font-normal">
                    <img src={logo} alt="logo" className="h-auto w-auto"/>
                </div>
            </a>
            <nav className="flex gap-8 flex-grow justify-center">
                <a href="#aboutus" className="text-black text-lg hover:text-accent">About us</a>
                <a href="/products" className="text-black text-lg hover:text-accent">Our Product</a>
                <a href="#" className="text-black text-2xl">
                    <span>🛒</span>
                </a>
            </nav>
        </header>
    );
}

export default Navbar;
