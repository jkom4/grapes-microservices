import {orderAPI} from "./httpCommon";
import Order from "../utils/models/Order";

 const fetchOrderHistory = async (userId: number): Promise<Order[]> => {
    try {
        const url = `${orderAPI.baseURL}${orderAPI.endpoints.orderHistory(userId)}`;
        const response = await fetch(url, {
            headers: {
                "Content-Type": "application/json",

            },
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status} - ${response.statusText}`);
        }

        const data = await response.json();
        return data.map((item: any) => Order.parse ? Order.parse(item) : item);
    } catch (err) {
        throw new Error(err instanceof Error ? err.message : "An error occurred while fetching order history");
    }
};

export default {
    fetchOrderHistory,
};