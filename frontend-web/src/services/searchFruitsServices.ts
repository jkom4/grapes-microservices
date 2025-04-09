import Article from "../utils/models/Articles";
import { searchArticlesAPI } from "./httpCommon";

// Fetches articles based on a search query with client-side pagination
const searchArticles = async (
    query: string, // Search query string
    page: number, // Current page number (0-based index)
    size: number // Number of articles per page
): Promise<{ content: Article[]; totalPages: number }> => {
    try {
        // Construct the API URL with search query and pagination parameters
        const url = `${searchArticlesAPI.baseURL}${searchArticlesAPI.endpoints.search(query)}&page=${page}&size=${size}`;

        // Make the HTTP request to the API
        const response = await fetch(url);

        // Check if the response is successful
        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status} - ${response.statusText}`);
        }

        // Parse the JSON response
        const data = await response.json();

        // Since the API returns a raw array, process it directly
        const allArticles: Article[] = data.map((item: any) => Article.parse(item));

        // Perform client-side pagination
        const start = page * size; // Calculate the starting index
        const end = start + size; // Calculate the ending index
        const paginatedArticles = allArticles.slice(start, end); // Extract the paginated subset

        // Return the paginated articles and total pages
        return {
            content: paginatedArticles, // Paginated list of articles
            totalPages: Math.ceil(allArticles.length / size), // Total pages calculated from total articles
        };
    } catch (err) {
        // Handle and rethrow any errors with a meaningful message
        throw new Error(err instanceof Error ? err.message : "An error occurred during search");
    }
};

export default searchArticles;