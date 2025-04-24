import React, { createContext, useContext, useEffect, useState } from "react";
import { cartService } from "../services/cartService";
import { useAuth } from "../features/AuthContext";
import { toast } from "react-toastify";

interface CartContextType {
    orderId: number | null;
    setOrderId: (orderId: number | null) => void;
    initializeCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [orderId, setOrderId] = useState<number | null>(null);
    const { sub } = useAuth();

    const initializeCart = async () => {
        try {
            if (!sub) {
                console.warn("User ID (sub) not available, cannot initialize cart");
                toast.error("Please log in to initialize your cart.", {
                    position: "top-right",
                    autoClose: 5000,
                });
                return;
            }

            console.log("Attempting to initialize cart with sub:", sub);

            let dynamicOrderId = localStorage.getItem("orderId");
            if (!dynamicOrderId) {
                const initResponse = await cartService.initializeCart(sub);
                console.log("Initialized cart with orderId:", initResponse.id, "userId:", initResponse.userId);
                if (initResponse.userId !== sub) {
                    console.warn("Mismatch: Expected userId:", sub, "but received:", initResponse.userId);
                    toast.warn("Error: Returned userId does not match.", {
                        position: "top-right",
                        autoClose: 5000,
                    });
                }
                dynamicOrderId = initResponse.id.toString();
                localStorage.setItem("orderId", dynamicOrderId);
                setOrderId(initResponse.id);
            } else {
                setOrderId(parseInt(dynamicOrderId, 10));
            }
        } catch (err) {
            console.error("Error initializing cart:", err);
            toast.error("Failed to initialize cart. Please try again.", {
                position: "top-right",
                autoClose: 5000,
            });
        }
    };

    useEffect(() => {
        initializeCart();
    }, [sub]);

    return (
        <CartContext.Provider value={{ orderId, setOrderId, initializeCart }}>
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error("useCart must be used within a CartProvider");
    }
    return context;
};