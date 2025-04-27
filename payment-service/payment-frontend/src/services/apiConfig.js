const API_GATEWAY_URL = 'http://89.168.47.217:8090/api';

// Adjust base paths to match backend controller mappings
export const AUTH_API_URL = `${API_GATEWAY_URL}`; // Base path for authentication service (LoginController is at /login)
export const PAYMENT_API_URL = `${API_GATEWAY_URL}`; // Base path for payment/verification service (PaymentController is at /payment)

// You can also export the gateway URL if needed elsewhere
// export const GATEWAY_URL = API_GATEWAY_URL;