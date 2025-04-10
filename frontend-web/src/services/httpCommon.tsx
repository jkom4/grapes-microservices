const getArticlesAPI = {
    baseURL: 'http://localhost:8092',
    endpoints: {
        articles: '/clm/articles',
        availableArticles: '/clm/articles/available',
        articleById: (id: number) => `/clm/articles/${id}`,
    },
};

const searchArticlesAPI = {
    baseURL: 'http://localhost:8092',
    endpoints: {
        search: (query: string) => `/clm/articles/search?name=${encodeURIComponent(query)}`,
    },
};

export { getArticlesAPI, searchArticlesAPI };
