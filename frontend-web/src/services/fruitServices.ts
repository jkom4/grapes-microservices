import Article from "../utils/models/Articles";
import {getArticlesAPI} from "./httpCommon";

const fetchFruits = async (limit: number): Promise<Article[]> => {
    try {
        const response = await fetch(`${getArticlesAPI.baseURL}${getArticlesAPI.endpoints.articles}`);

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status} - ${response.statusText}`);
        }

        const text = await response.text();

        const contentType = response.headers.get("Content-Type");
        if (!contentType || !contentType.includes("application/json")) {
            throw new Error(`Response is not JSON: ${text.slice(0, 100)}...`);
        }

        const data = JSON.parse(text);

        return limit ? data.slice(0, limit).map((item: any) => Article.parse(item)) : data.map((item: any) => Article.parse(item));
    } catch (err) {
        throw new Error(err instanceof Error ? err.message : "An error occurred");
    }
};

export default fetchFruits;
