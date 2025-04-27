import { useEffect, useState } from "react";
import Article from "../utils/models/Articles";
import { fetchFruits } from "../services/fruitServices";
import searchArticles from "../services/searchFruitsServices";

const useArticles = (currentPage: number, searchQuery: string, articlesPerPage: number, limit: number) => {
    const [articles, setArticles] = useState<Article[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [totalPages, setTotalPages] = useState<number>(0);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError(null);
            try {
                if (limit > 0) {
                    // Fetch limited articles if limit is specified
                    const { content } = await fetchFruits(0, limit);
                    setArticles(content);
                    setTotalPages(1);
                } else if (searchQuery === "") {
                    // Fetch all articles for the current page
                    const { content, totalPages } = await fetchFruits(currentPage - 1, articlesPerPage);
                    setArticles(content);
                    setTotalPages(totalPages);
                } else {
                    // Fetch search results for the current page
                    const { content, totalPages } = await searchArticles(searchQuery, currentPage - 1, articlesPerPage);
                    setArticles(content);
                    setTotalPages(totalPages);
                }
            } catch (err) {
                console.error("Error fetching data:", err);
                setError(err instanceof Error ? err.message : "An error occurred");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [currentPage, searchQuery, articlesPerPage, limit]);

    return { articles, loading, error, totalPages };
};

export default useArticles;