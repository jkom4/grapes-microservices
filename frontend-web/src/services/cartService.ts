import { cartAPI } from "./httpCommon";
import CartItemModel from "../utils/models/CartItem";

interface InitCartResponse {
    id: number;
    code?: number;
    userId?: string; // userId is now a string to match sub
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
    async initializeCart(sub: string): Promise<InitCartResponse> {
        console.log("User ID (sub) received in cartService.initializeCart:", sub);
        const payload = { userId: sub }; // Changed from { sub } to { userId: sub }

        console.log("Payload sent to initialize cart:", payload);

        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.init}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        });

        console.log("Cart initialization HTTP status:", response.status);
        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error initializing cart for userId:", sub, "Details:", errorDetails);
            throw new Error(`Failed to initialize cart. Details: ${errorDetails}`);
        }

        const data = await response.json();
        console.log("Cart initialization response for userId:", sub, "Response:", data);
        console.log("Received userId in response:", data.userId);
        return data;
    },

    // Add an item to the cart
    async addItemToCart(
        orderId: number,
        articleId: number,
        quantityKg: number,
        quantity: number
    ): Promise<CartResponse> {
        const payload = { orderId, articleId, quantityKg, quantity };
        console.log("Adding item to cart for orderId:", orderId, "Payload:", payload);

        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.add}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        });

        console.log("Add item to cart HTTP status:", response.status);
        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error adding item to cart for orderId:", orderId, "Details:", errorDetails);
            throw new Error(`Failed to add item to cart. Details: ${errorDetails}`);
        }

        const data = await response.json();
        console.log("Add item to cart response for orderId:", orderId, "Response:", data);
        return data;
    },

    // Fetch cart by order ID
    async fetchCart(orderId: number): Promise<CartResponse> {
        console.log("Fetching cart for orderId:", orderId);

        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.get(orderId)}`);
        console.log("Fetch cart HTTP status:", response.status);

        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error fetching cart for orderId:", orderId, "Details:", errorDetails);
            throw new Error(`Failed to fetch cart items. Details: ${errorDetails}`);
        }

        const data = await response.json();
        console.log("Fetch cart response for orderId:", orderId, "Response:", data);
        return data;
    },

    // Process payment for the cart
    async processPayment(
        orderId: number,
        address: string,
        phoneNumber: string,
        customerName: string
    ): Promise<void> {
        const payload = { orderId, address, phoneNumber, customerName };
        console.log("Processing payment for orderId:", orderId, "Payload:", payload);

        const url = `${cartAPI.baseURL}${cartAPI.endpoints.pay}`;
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        });

        console.log("Process payment HTTP status:", response.status);
        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error processing payment for orderId:", orderId, "Details:", errorDetails);
            throw new Error(`Payment failed. Details: ${errorDetails}`);
        }

        console.log("Payment processed successfully for orderId:", orderId);
    },

    // Remove an item from the cart
    async removeItem(orderId: number, itemId: number): Promise<void> {
        console.log("Removing item from cart for orderId:", orderId, "itemId:", itemId);

        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.remove(orderId, itemId)}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
            },
        });

        console.log("Remove item HTTP status:", response.status);
        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error removing item for orderId:", orderId, "itemId:", itemId, "Details:", errorDetails);
            throw new Error(`Failed to remove item. Details: ${errorDetails}`);
        }

        console.log("Item removed successfully for orderId:", orderId, "itemId:", itemId);
    },

    // Clear the cart
    async clearCart(orderId: number): Promise<void> {
        console.log("Clearing cart for orderId:", orderId);

        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.clear(orderId)}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
            },
        });

        console.log("Clear cart HTTP status:", response.status);
        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error clearing cart for orderId:", orderId, "Details:", errorDetails);
            throw new Error(`Failed to clear cart. Details: ${errorDetails}`);
        }

        console.log("Cart cleared successfully for orderId:", orderId);
    },
};