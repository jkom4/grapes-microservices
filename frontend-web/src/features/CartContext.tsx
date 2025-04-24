// src/context/CartContext.tsx
import React, { createContext, useContext, useEffect, useState } from "react";
import { cartService } from "../services/cartService";

interface CartContextType {
    orderId: number | null;
    setOrderId: (orderId: number | null) => void;
    initializeCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [orderId, setOrderId] = useState<number | null>(null);
    const userId = 1;

    const initializeCart = async () => {
        try {
            let dynamicOrderId = localStorage.getItem("orderId");
            if (!dynamicOrderId) {
                const initResponse = await cartService.initializeCart(userId);
                dynamicOrderId = initResponse.id.toString();
                localStorage.setItem("orderId", dynamicOrderId);
                setOrderId(initResponse.id);
            } else {
                setOrderId(parseInt(dynamicOrderId, 10));
            }
        } catch (err) {
            console.error("Error to init cart:", err);
        }
    };

    useEffect(() => {
        initializeCart();
    }, []);

    return (
        <CartContext.Provider value={{ orderId, setOrderId, initializeCart }}>
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error("useCart will be inside CartProvider");
    }
    return context;
};