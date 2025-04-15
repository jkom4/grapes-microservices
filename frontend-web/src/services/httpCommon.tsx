// src/api/apiConfig.ts (or wherever your API routes are defined)
const BASE_URL = "http://localhost:8092";

const getArticlesAPI = {
    baseURL: BASE_URL,
    endpoints: {
        articles: "/clm/articles",
        availableArticles: "/clm/articles/available",
        articleById: (id: number) => `/clm/articles/${id}`,
        addArticle: "/clm/articles",
        updateArticle: (id: number) => `/clm/articles/${id}`,
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
        pay: (orderId: number | string) => `/clm/cart/pay/${orderId}`,
        remove: (itemId: number) => `/clm/cart/remove/${itemId}`,
        applyPromo: "/clm/cart/apply-promo",
        clear: (orderId: number | string) => `/clm/cart/clear/${orderId}`,
    },
};

export { getArticlesAPI, searchArticlesAPI, cartAPI };