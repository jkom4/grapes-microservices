// src/hooks/useArticle.ts
import { useEffect, useState } from "react";
import Article from "../utils/models/Articles";
import { fetchArticleById } from "../services/fruitServices";
import { useCart } from "../features/CartContext";

export const useArticleDetails = (id: string | undefined) => {
    const [article, setArticle] = useState<Article | null>(null);
    const [error, setError] = useState<string | null>(null);
    const { orderId } = useCart(); // orderId est number | null

    useEffect(() => {
        const fetchArticle = async () => {
            if (!id) {
                setError("Article ID is missing");
                return;
            }

            try {
                const articleId = parseInt(id);
                const articleData = await fetchArticleById(articleId);
                setArticle(articleData);
            } catch (err) {
                setError(err instanceof Error ? err.message : "An unknown error occurred");
            }
        };

        fetchArticle();
    }, [id]);

    return { article, orderId, error, setError };
};