import { cartAPI } from "./httpCommon";
import CartItemModel from "../utils/models/CartItem";

interface InitCartResponse {
    id: number;
    code?: number;
    userId?: number;
    facturePath?: string | null;
    totalPrice?: number | null;
    createdAt?: string;
    orderItems?: any[] | null;
    paid?: boolean;
    finished?: boolean;
}

export interface CartResponse {
    items: CartItemModel[];
    totalPrice: number;
}

export const cartService = {
    // Initialize a cart for a user
    async initializeCart(userId: number): Promise<InitCartResponse> {
        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.init}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ userId }),
        });

        if (!response.ok) {
            const errorDetails = await response.text();
            throw new Error(`Failed to initialize cart. Details: ${errorDetails}`);
        }

        return response.json();
    },

    // Add an item to the cart
    async addItemToCart(
        orderId: number,
        articleId: number,
        quantityKg: number,
        quantity: number
    ): Promise<CartResponse> {
        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.add}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ orderId, articleId, quantityKg, quantity }),
        });

        if (!response.ok) {
            const errorDetails = await response.text();
            throw new Error(`Failed to add item to cart. Details: ${errorDetails}`);
        }

        return response.json();
    },

    // Fetch cart by order ID
    async fetchCart(orderId: number): Promise<CartResponse> {
        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.get(orderId)}`);
        if (!response.ok) {
            const errorDetails = await response.text();
            throw new Error(`Failed to fetch cart items. Details: ${errorDetails}`);
        }
        return response.json();
    },

    // Process payment for the cart
    async processPayment(
        orderId: number,
        address: string,
        phoneNumber: string,
        customerName: string
    ): Promise<void> {
        const url = `${cartAPI.baseURL}${cartAPI.endpoints.pay}`;
        const body = {
            orderId,
            address,
            phoneNumber,
            customerName,
        };

        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(body),
        });

        if (!response.ok) {
            const errorDetails = await response.text();
            throw new Error(`Payment failed. Details: ${errorDetails}`);
        }
    },

    // Remove an item from the cart
    async removeItem(orderId: number, itemId: number): Promise<void> {
        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.remove(orderId, itemId)}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
            },
        });
        if (!response.ok) {
            const errorDetails = await response.text();
            throw new Error(`Failed to remove item. Details: ${errorDetails}`);
        }
    },

    // Clear the cart
    async clearCart(orderId: number): Promise<void> {
        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.clear(orderId)}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
            },
        });
        if (!response.ok) {
            const errorDetails = await response.text();
            throw new Error(`Failed to clear cart. Details: ${errorDetails}`);
        }
    },
};