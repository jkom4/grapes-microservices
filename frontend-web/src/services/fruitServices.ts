import Article from "../utils/models/Articles";
import { getArticlesAPI } from "./httpCommon";

// Fetches paginated articles from the API
export const fetchFruits = async (
    page: number, // Current page number (0-based index)
    size: number // Number of articles per page
): Promise<{ content: Article[]; totalPages: number }> => {
    try {
        // Construct the API URL with pagination parameters
        const url = `${getArticlesAPI.baseURL}${getArticlesAPI.endpoints.availableArticles}?page=${page}&size=${size}`;

        // Make the HTTP request to the API
        const response = await fetch(url);

        // Check if the response is successful
        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status} - ${response.statusText}`);
        }

        // Parse the JSON response
        const data = await response.json();

        // Map the response content to Article objects
        const articles: Article[] = data.content.map((item: any) => Article.parse(item));

        // Return the paginated articles and total pages
        return {
            content: articles, // List of parsed articles
            totalPages: data.totalPages, // Total number of pages available
        };
    } catch (err) {
        // Handle and rethrow any errors with a meaningful message
        throw new Error(err instanceof Error ? err.message : "An error occurred");
    }
};

export const fetchArticleById = async (id: number): Promise<Article> => {
    try {
        const url = `${getArticlesAPI.baseURL}${getArticlesAPI.endpoints.articleById(id)}`;
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status} - ${response.statusText}`);
        }

        const data = await response.json();
        return Article.parse(data);  // Assuming Article.parse() is a static method that parses the response
    } catch (err) {
        throw new Error(err instanceof Error ? err.message : "An error occurred while fetching the article");
    }
};

// Add a new article
export const addArticle = async (article: Article): Promise<void> => {
    try {
        const url = `${getArticlesAPI.baseURL}${getArticlesAPI.endpoints.addArticle}`;
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(article),
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status} - ${response.statusText}`);
        }
    } catch (err) {
        throw new Error(err instanceof Error ? err.message : "An error occurred while adding the article");
    }
};

// Update an article
export const updateArticle = async (id: number, article: Article): Promise<void> => {
    try {
        const url = `${getArticlesAPI.baseURL}${getArticlesAPI.endpoints.updateArticle(id)}`;
        const response = await fetch(url, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(article),
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status} - ${response.statusText}`);
        }
    } catch (err) {
        throw new Error(err instanceof Error ? err.message : "An error occurred while updating the article");
    }
};

export default {
    fetchFruits,
    fetchArticleById,
    addArticle,
    updateArticle
};