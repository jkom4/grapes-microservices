import React, { createContext, useContext, useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
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
    const [hasInitialized, setHasInitialized] = useState(false);
    const { sub } = useAuth();
    const location = useLocation();

    const initializeCart = async (forceNew: boolean = false) => {
        try {
            if (!sub) {
                console.warn("User ID (sub) not available, cannot initialize cart");
                toast.error("Please log in to initialize your cart.", {
                    position: "top-right",
                    autoClose: 5000,
                });
                return;
            }

            let dynamicOrderId = localStorage.getItem("orderId");

            if (forceNew || !dynamicOrderId) {
                const initResponse = await cartService.initializeCart(sub);
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
            setHasInitialized(true);
        } catch (err) {
            console.error("Error initializing cart:", err);
            toast.error("Failed to initialize cart. Please try again.", {
                position: "top-right",
                autoClose: 5000,
            });
        }
    };

    useEffect(() => {
        if (sub && !hasInitialized) {
            initializeCart();
        }
    }, [sub, hasInitialized]);

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
