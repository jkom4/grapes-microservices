// Example: http://localhost:8093
const API_GATEWAY_URL = 'http://localhost:8093';

// Specific base URLs if services are hosted independently or have different base paths
// Adjust the paths like '/api' based on your actual backend routing
export const AUTH_API_URL = `${API_GATEWAY_URL}/api`; // Base path for authentication service
export const PAYMENT_API_URL = `${API_GATEWAY_URL}/api`; // Base path for payment/verification service

// You can also export the gateway URL if needed elsewhere
// export const GATEWAY_URL = API_GATEWAY_URL;