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
        pay: (orderId: number | string, params: { address: string; phoneNumber: string; customerName: string; country: string; postalCode: string }) =>
            `/clm/cart/pay/${orderId}?address=${encodeURIComponent(params.address)}&phoneNumber=${encodeURIComponent(params.phoneNumber)}&customerName=${encodeURIComponent(params.customerName)}&country=${encodeURIComponent(params.country)}&postalCode=${encodeURIComponent(params.postalCode)}`,
        remove: (itemId: number) => `/clm/cart/remove/${itemId}`,
        applyPromo: "/clm/cart/apply-promo",
        clear: (orderId: number | string) => `/clm/cart/clear/${orderId}`,
    },
};

export { getArticlesAPI, searchArticlesAPI, cartAPI };