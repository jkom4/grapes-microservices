const BASE_URL = "http://localhost:8092";
const BASE_URL_AUTH = "http://localhost:8091";
const BASE_URL_PAYMENT = "http://localhost:8093";
const BASE_TO_PAY = "http://localhost:3002";
const MY_BASE_URL = "http://localhost:3000";

const getArticlesAPI = {
    baseURL: BASE_URL,
    endpoints: {
        articles: "/clm/articles",
        availableArticles: "/clm/articles/available",
        articleById: (id: number) => `/clm/articles/${id}`,
        addArticle: "/clm/articles",
        updateArticle: (id: number) => `/clm/articles/${id}`,
        deleteArticle: (id: number) => `/clm/articles/${id}`,
    },
};

const searchArticlesAPI = {
    baseURL: BASE_URL,
    endpoints: {
        search: (query: string) => `/clm/articles/search?name=${encodeURIComponent(query)}`,
    },
};

const cartAPI = {
    baseURL: BASE_URL,
    endpoints: {
        init: "/clm/cart/init",
        add: "/clm/cart/add",
        get: (orderId: number | string) => `/clm/cart/${orderId}`,
        remove: (orderId: number | string, itemId: number) => `/clm/cart/remove/${orderId}/${itemId}`,
        clear: (orderId: number | string) => `/clm/cart/clear/${orderId}`,
    },
};

const paymentAPI = {
    baseURL: BASE_URL_PAYMENT,
    endpoints: {
        pay: "/login/payment-initiate",
    }

}

const redirectionService = {
    baseURL: BASE_TO_PAY,
    endpoints: {
        toPayment: "/login"
    }
}

const myService = {
    baseURL: MY_BASE_URL,
}

const orderAPI = {
    baseURL: BASE_URL,
    endpoints: {
        orderHistory: (userId: string) => `/cll/orders/history/${userId}`,
    },
};

const deliveryAPI = {
    baseURL: BASE_URL,
    endpoints: {
        getDeliveryStatus: (orderId: number | string) => `/cll/deliveries/status/${orderId}`,
    },
};

const authenticationAPI = {
    baseURL: BASE_URL_AUTH,
    endpoints: {
        logout: "/auth/logout",
    },
};

const generateLoginUrl = (stateAuthentication: number | null) => {
    const baseUrl = "http://localhost:3001";
    const redirectUri = "http://localhost:3000";
    return `${baseUrl}?redirect_uri=${encodeURIComponent(redirectUri)}&state=${stateAuthentication !== null ? stateAuthentication : 'default'}`;
};

export { getArticlesAPI, searchArticlesAPI, cartAPI, paymentAPI, myService, redirectionService, orderAPI, deliveryAPI, authenticationAPI, generateLoginUrl };