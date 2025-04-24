import { useEffect, useState } from "react";
import Article from "../utils/models/Articles";
import { fetchArticleById } from "../services/fruitServices";
import { useCart } from "../features/CartContext";
import { cartService } from "../services/cartService";
import { toast } from "react-toastify";

export const useArticleDetails = (id: string | undefined, sub: string | null) => {
    const [article, setArticle] = useState<Article | null>(null);
    const [error, setError] = useState<string | null>(null);
    const { orderId, setOrderId, initializeCart } = useCart();

    console.log("User ID (sub) in useArticleDetails:", sub);
    console.log("Current orderId in useArticleDetails:", orderId);

    useEffect(() => {
        const fetchArticleAndInitializeCart = async () => {
            try {
                if (!id) {
                    throw new Error("Article ID is missing");
                }
                if (!sub) {
                    throw new Error("User ID (sub) is missing");
                }

                // Fetch article details
                const articleId = parseInt(id, 10);
                const articleData = await fetchArticleById(articleId);
                setArticle(articleData);

                // Check if cart is initialized; if not, initialize it
                if (!orderId) {
                    console.log("No orderId found, initializing cart with sub:", sub);
                    let dynamicOrderId = localStorage.getItem("orderId");
                    if (!dynamicOrderId) {
                        const initResponse = await cartService.initializeCart(sub);
                        console.log("Initialized cart with orderId:", initResponse.id, "userId:", initResponse.userId);
                        if (initResponse.userId !== sub) {
                            console.warn("Mismatch: Expected userId:", sub, "but received:", initResponse.userId);
                            toast.warn("Error: Returned userId does not match.", {
                                position: "top-right",
                                autoClose: 5000,
                            });
                        }
                        dynamicOrderId = initResponse.id.toString();
                        localStorage.setItem("orderId", dynamicOrderId);
                        setOrderId(initResponse.id);
                    } else {
                        setOrderId(parseInt(dynamicOrderId, 10));
                    }
                }
            } catch (err) {
                setError(err instanceof Error ? err.message : "An unknown error occurred");
                toast.error(err instanceof Error ? err.message : "An unknown error occurred", {
                    position: "top-right",
                    autoClose: 5000,
                });
            }
        };

        fetchArticleAndInitializeCart();
    }, [id, sub, orderId, setOrderId]);

    return { article, orderId, error, setError };
};