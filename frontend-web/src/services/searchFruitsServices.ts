import Article from "../utils/models/Articles";
import {searchArticlesAPI} from "./httpCommon";

const searchArticles = async (query: string): Promise<Article[]> => {
    try {
        const response = await fetch(`${searchArticlesAPI.baseURL}${searchArticlesAPI.endpoints.search.replace('${query}', query)}`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            const data = await response.json();
            return data.map((item: any) => Article.parse(item));
        } else {
            throw new Error("Expected JSON response, but got something else.");
        }
    } catch (err) {
        throw new Error(err instanceof Error ? err.message : "An error occurred");
    }
};

export default searchArticles;
