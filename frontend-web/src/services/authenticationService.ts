import { authenticationAPI } from "./httpCommon";

const headers = {
    "Content-Type": "application/json",
};

const handleConnectionError = (error: any) => {
    console.error("Connection error:", error);
};

export const authenticationService = {
    logout: async (token: string): Promise<Response> => {
        if (!token) {
            throw new Error("Token is required for logout");
        }

        try {
            const res = await fetch(`${authenticationAPI.baseURL}${authenticationAPI.endpoints.logout}`, {
                method: "POST",
                headers: {
                    ...headers,
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                const errorText = await res.json();
                throw new Error(errorText.message || "Logout failed");
            }

            return res;
        } catch (error: any) {
            handleConnectionError(error);
            console.error("[Logout] Error:", error);
            throw error;
        }
    },
};
