const BASE_URL = "http://89.168.47.217:8090/api";
const BASE_URL_AUTH = "http://89.168.47.217:8090/api";
const BASE_URL_PAYMENT = "http://89.168.47.217:8090/api";
const BASE_TO_PAY = "http://79.76.108.164:82";
const MY_BASE_URL = "http://79.76.108.164:80";
const BASE_FRONT_AUTH = "http://79.76.108.164:81";

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
        pay: "/payment/login/payment-initiate",
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
    const baseUrl = BASE_FRONT_AUTH;
    return `${baseUrl}?redirect_uri=${encodeURIComponent(MY_BASE_URL)}&state=${stateAuthentication !== null ? stateAuthentication : 'default'}`;
};

export { getArticlesAPI, searchArticlesAPI, cartAPI, paymentAPI, myService, redirectionService, orderAPI, deliveryAPI, authenticationAPI, generateLoginUrl };