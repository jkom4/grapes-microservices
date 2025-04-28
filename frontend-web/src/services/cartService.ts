
import { cartAPI, myService, paymentAPI, redirectionService } from "./httpCommon";
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
    async initializeCart(sub: string): Promise<InitCartResponse> {

        const payload = {userId: sub};

        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.init}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error initializing cart for userId:", sub, "Details:", errorDetails);
            throw new Error(`Failed to initialize cart. Details: ${errorDetails}`);
        }

        const data = await response.json();

        return data;
    },

    async addItemToCart(
        orderId: number,
        articleId: number,
        quantityKg: number,
        quantity: number

        const payload = {orderId, articleId, quantityKg, quantity};

        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.add}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error adding item to cart for orderId:", orderId, "Details:", errorDetails);
            throw new Error(`Failed to add item to cart. Details: ${errorDetails}`);
        }

        const data = await response.json();

        return data;
    },

    async fetchCart(orderId: number): Promise<CartResponse> {


        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.get(orderId)}`);

        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error fetching cart for orderId:", orderId, "Details:", errorDetails);
            throw new Error(`Failed to fetch cart items. Details: ${errorDetails}`);
        }

        const data = await response.json();

        return data;
    },

    async processPayment(
        orderId: number,
        address: string,
        phoneNumber: string,
        customerName: string,
        amount: number
    ): Promise<string> {

    
        // First API call: Original paymentAPI call
        const redirectUrl = `${myService.baseURL}`;
        const paymentPayload = {

            amount: amount,
            merchantId: "grapes",
            redirectUrl: redirectUrl,
        };


        const paymentUrl = `${paymentAPI.baseURL}${paymentAPI.endpoints.pay}`;
        const paymentResponse = await fetch(paymentUrl, {

            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(paymentPayload),
            credentials: 'include'
        });

        if (!paymentResponse.ok) {
            const errorDetails = await paymentResponse.text();
            console.error("Error processing payment for orderId:", orderId, "Status:", paymentResponse.status, "Details:", errorDetails);
            throw new Error(`Payment failed (paymentAPI). Status: ${paymentResponse.status}, Details: ${errorDetails}`);
        }

        const paymentText = await paymentResponse.text();
        console.log(`paymentAPI raw response for orderId: ${orderId}:`, paymentText);

        // Second API call: New cartAPI call
        const cartPayload = { orderId, address, phoneNumber, customerName };
        console.log(`cartAPI payload:`, cartPayload);

        const cartUrl = `${cartAPI.baseURL}${cartAPI.endpoints.pay}`;
        const cartResponse = await fetch(cartUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(cartPayload),
            credentials: 'include'
        });

        if (!cartResponse.ok) {
            const errorDetails = await cartResponse.text();
            console.error("Error processing payment for orderId:", orderId, "Status:", cartResponse.status, "Details:", errorDetails);
            throw new Error(`Payment failed (cartAPI). Status: ${cartResponse.status}, Details: ${errorDetails}`);
        }

        const cartText = await cartResponse.text();
        console.log(`cartAPI raw response for orderId: ${orderId}:`, cartText);


        return `${redirectionService.baseURL}${redirectionService.endpoints.toPayment}`;
    },

    async removeItem(orderId: number, itemId: number): Promise<void> {

        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.remove(orderId, itemId)}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
            },
        });

        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error removing item for orderId:", orderId, "itemId:", itemId, "Details:", errorDetails);
            throw new Error(`Failed to remove item. Details: ${errorDetails}`);
        }


        console.log(`Item removed successfully for orderId: ${orderId}, itemId: ${itemId}`);
    },

    async clearCart(orderId: number): Promise<void> {
        console.log(`Attempting tographql clear cart for orderId: ${orderId}`);

        const response = await fetch(`${cartAPI.baseURL}${cartAPI.endpoints.clear(orderId)}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
            },
        });

        if (!response.ok) {
            const errorDetails = await response.text();
            console.error("Error clearing cart for orderId:", orderId, "Details:", errorDetails);
            throw new Error(`Failed to clear cart. Details: ${errorDetails}`);
        }

        console.log(`Cart cleared successfully for orderId: ${orderId}`);
    }

};

