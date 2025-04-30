import { useEffect, useState } from "react";
import Article from "../utils/models/Articles";
import { fetchArticleById } from "../services/fruitServices";
import { useCart } from "../features/CartContext";
import { toast } from "react-toastify";

export const useArticleDetails = (id: string | undefined, sub: string | null) => {
    const [article, setArticle] = useState<Article | null>(null);
    const [error, setError] = useState<string | null>(null);
    const { orderId, initializeCart } = useCart();

    useEffect(() => {
        const fetchArticle = async () => {
            try {
                if (!id) {
                    throw new Error("Article ID is missing");
                }

                // Fetch article details
                const articleId = parseInt(id, 10);
                const articleData = await fetchArticleById(articleId);
                setArticle(articleData);

                // If no orderId, trigger CartProvider's initializeCart
                if (!orderId) {
                    await initializeCart();
                }
            } catch (err) {
                setError(err instanceof Error ? err.message : "An unknown error occurred");
                toast.error(err instanceof Error ? err.message : "An unknown error occurred", {
                    position: "top-right",
                    autoClose: 5000,
                });
            }
        };

        fetchArticle();
    }, [id, sub, orderId, initializeCart]);

    return { article, orderId, error, setError };
};