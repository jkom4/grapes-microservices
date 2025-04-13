const apiUrl = process.env.REACT_APP_API_URL_SALES || 'http://localhost:8092';
const getArticlesAPI = {
    baseURL: apiUrl,
    endpoints: {
        articles: '/clm/articles',
        availableArticles: '/clm/articles/available',
    },
};

const searchArticlesAPI = {
    baseURL: apiUrl,
    endpoints: {
        search: (query: string) => `/clm/articles/search?name=${encodeURIComponent(query)}`,
    },
};

export { getArticlesAPI, searchArticlesAPI };
