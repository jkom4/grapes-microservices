const getArticlesAPI = {
    baseURL: 'http://localhost:8092',
    endpoints: {
        articles: '/clm/articles',
    },
};

const searchArticlesAPI = {
    baseURL: 'http://localhost:8092',
    endpoints: {
        search: '/clm/articles/search?name=${query}',
    },
};

export { getArticlesAPI, searchArticlesAPI };
