// src/api/apiConfig.ts
const BASE_URL = "http://localhost:8092";

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
        pay: "/clm/cart/pay",
        remove: (orderId: number | string, itemId: number) => `/clm/cart/remove/${orderId}/${itemId}`,
        clear: (orderId: number | string) => `/clm/cart/clear/${orderId}`,
    },
};

const orderAPI = {
    baseURL: BASE_URL,
    endpoints: {
        orderHistory: (userId: number) => `/cll/orders/history/${userId}`,
    },
};

const deliveryAPI = {
    baseURL: BASE_URL,
    endpoints: {
        getDeliveryStatus: (orderId: number | string) => `/cll/deliveries/status/${orderId}`,
    },
};

export { getArticlesAPI, searchArticlesAPI, cartAPI, orderAPI, deliveryAPI };