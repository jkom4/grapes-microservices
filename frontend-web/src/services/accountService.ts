import {orderAPI} from "./httpCommon";
import Order from "../utils/models/Order";

export const fetchOrderHistory = async (sub: string): Promise<Order[]> => {
    try {
        if (!sub || !/^[a-f0-9]{24}$/.test(sub)) {
            throw new Error("Invalid User ID (sub) format");
        }

        const url = `${orderAPI.baseURL}${orderAPI.endpoints.orderHistory(sub)}`;

        const response = await fetch(url, {
            headers: {
                "Content-Type": "application/json",
            },
        });

        if (!response.ok) {
            const errorDetails = await response.text();
            throw new Error(`HTTP Error: ${response.status} - ${response.statusText}. Details: ${errorDetails}`);
        }

        const data = await response.json();
        return data.map((item: any) => (Order.parse ? Order.parse(item) : item));
    } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "An error occurred while fetching order history";
        console.error("Error fetching order history:", errorMessage);
        throw new Error(errorMessage);
    }
};

export default {
    fetchOrderHistory,
};