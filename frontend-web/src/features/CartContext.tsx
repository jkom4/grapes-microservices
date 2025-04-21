// src/context/CartContext.tsx
import React, { createContext, useContext, useEffect, useState } from "react";
import { cartService } from "../services/cartService";

interface CartContextType {
    orderId: number | null; // Changé de string | null à number | null
    setOrderId: (orderId: number | null) => void;
    initializeCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [orderId, setOrderId] = useState<number | null>(null);
    const userId = 1; // À remplacer par une authentification réelle

    const initializeCart = async () => {
        try {
            let dynamicOrderId = localStorage.getItem("orderId");
            if (!dynamicOrderId) {
                const initResponse = await cartService.initializeCart(userId);
                dynamicOrderId = initResponse.id.toString(); // Stocker en chaîne dans localStorage
                localStorage.setItem("orderId", dynamicOrderId);
                setOrderId(initResponse.id); // Stocker comme nombre dans l'état
            } else {
                setOrderId(parseInt(dynamicOrderId, 10)); // Convertir la chaîne de localStorage en nombre
            }
        } catch (err) {
            console.error("Erreur lors de l'initialisation du panier:", err);
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
        throw new Error("useCart doit être utilisé à l'intérieur d'un CartProvider");
    }
    return context;
};