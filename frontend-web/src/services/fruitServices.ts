import Article from "../utils/models/Articles";
import { getArticlesAPI } from "./httpCommon";

// Fetches paginated articles from the API
const fetchFruits = async (
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

export default fetchFruits;