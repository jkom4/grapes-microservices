import { useEffect, useState } from "react";
import Article from "../utils/models/Articles";
import { fetchArticleById } from "../services/fruitServices";
import { cartService } from "../services/cartService";

export const useArticleDetails = (id: string | undefined, userId: number) => {
    const [article, setArticle] = useState<Article | null>(null);
    const [orderId, setOrderId] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const initializeCartAndFetchArticle = async () => {
            if (!id) {
                setError("Article ID is missing");
                return;
            }

            try {
                // Check if orderId exists in localStorage
                let dynamicOrderId = localStorage.getItem("orderId");
                if (!dynamicOrderId) {
                    // Initialize cart and retrieve orderId
                    const initResponse = await cartService.initializeCart(userId);
                    dynamicOrderId = initResponse.id.toString();
                    localStorage.setItem("orderId", dynamicOrderId);
                }
                setOrderId(dynamicOrderId);

                // Fetch article by ID
                const articleId = parseInt(id);
                const articleData = await fetchArticleById(articleId);
                setArticle(articleData);
            } catch (err) {
                // Handle errors during cart initialization or article fetching
                setError(err instanceof Error ? err.message : "An unknown error occurred");
            }
        };

        initializeCartAndFetchArticle();
    }, [id, userId]);

    return { article, orderId, error, setError };
};