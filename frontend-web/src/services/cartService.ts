// src/services/cartService.ts
import { cartAPI } from "./httpCommon"; // Adjust path if needed
import CartItemModel from "../utils/models/CartItem";

interface InitCartResponse {
    orderId: number;
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
    async fetchCart(orderId: string): Promise<CartResponse> {
        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.get(orderId)}`);
        if (!response.ok) {
            const errorDetails = await response.text();
            throw new Error(`Failed to fetch cart items. Details: ${errorDetails}`);
        }
        return response.json();
    },

    // Process payment for the cart
    async processPayment(orderId: string): Promise<void> {
        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.pay(orderId)}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
        });
        if (!response.ok) {
            const errorDetails = await response.text();
            throw new Error(`Payment failed. Details: ${errorDetails}`);
        }
    },

    // Remove an item from the cart
    async removeItem(itemId: number): Promise<void> {
        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.remove(itemId)}`, {
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

    // Apply a promo code to the cart
    async applyPromoCode(orderId: string, promoCode: string): Promise<CartResponse> {
        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.applyPromo}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ orderId, promoCode }),
        });
        if (!response.ok) {
            const errorDetails = await response.text();
            throw new Error(`Failed to apply promo code. Details: ${errorDetails}`);
        }
        return response.json();
    },

    // Clear the cart
    async clearCart(orderId: string): Promise<void> {
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