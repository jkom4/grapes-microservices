import React, { useEffect, useState } from "react";
import Article from "../../utils/models/Articles";
import { useLanguage } from "../../features/LanguageContext";
import { fetchFruits } from "../../services/fruitServices";
import CardComponent from "../../components/CardComponent"; // Import the CardComponent

function DisplayProductSection({ limit = 6 }: { limit?: number }) {
    const [articles, setArticles] = useState<Article[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const { language } = useLanguage();
    const [cartAnimation, setCartAnimation] = useState<{ id: number | null; x: number; y: number }>({
        id: null,
        x: 0,
        y: 0,
    });
    const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

    const text = {
        en: {
            header: "Specially for you",
            addToCartSuccess: "Item added to cart!",
            addToCartError: "Failed to add item to cart",
        },
        fr: {
            header: "Spécialement pour vous",
            addToCartSuccess: "Article ajouté au panier !",
            addToCartError: "Échec de l'ajout au panier",
        },
    };

    const handleAddToCart = async (article: Article, event: React.MouseEvent<HTMLButtonElement>) => {
        const orderId = 1; // Replace with actual order ID logic
        const articleId = article.id;
        const quantityKg = article.stockKg;
        const selectedQuantity = 1;

        if (!articleId) {
            console.error("Article ID is missing, cannot add to cart");
            setToast({ message: text[language].addToCartError, type: "error" });
            return;
        }

        // Trigger animation
        const button = event.currentTarget;
        const rect = button.getBoundingClientRect();
        setCartAnimation({
            id: article.id,
            x: rect.left + rect.width / 2,
            y: rect.top + rect.height / 2,
        });

        try {
            const response = await fetch("http://localhost:8092/clm/cart/add", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    orderId,
                    articleId,
                    quantityKg,
                    quantity: selectedQuantity,
                }),
            });

            if (!response.ok) {
                throw new Error("Failed to add item to cart");
            }

            const responseData = await response.json();
            console.log("Item added to cart successfully:", responseData);
            setToast({ message: text[language].addToCartSuccess, type: "success" });

            // Reset animation after a delay
            setTimeout(() => {
                setCartAnimation({ id: null, x: 0, y: 0 });
            }, 1000);
        } catch (error) {
            console.error("Error adding item to cart:", error);
            setToast({ message: text[language].addToCartError, type: "error" });
            setCartAnimation({ id: null, x: 0, y: 0 });
        }
    };

    useEffect(() => {
        const fetchData = async () => {
            try {
                const { content } = await fetchFruits(0, limit);
                setArticles(content);
                setLoading(false);
            } catch (err) {
                setError(err instanceof Error ? err.message : "An error occurred");
                setLoading(false);
            }
        };

        fetchData();
    }, [limit]);

    // Auto-dismiss toast after 3 seconds
    useEffect(() => {
        if (toast) {
            const timer = setTimeout(() => setToast(null), 3000);
            return () => clearTimeout(timer);
        }
    }, [toast]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <section className="bg-white py-8">
            <section className="hero flex justify-center items-center py-8">
                <div className="hero-content max-w-screen-lg flex justify-between items-center w-full px-4">
                    <h2 className="text-2xl font-semibold text-black border-b-4 border-accent pb-2 inline-block">
                        {text[language].header}
                    </h2>
                </div>
            </section>

            <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8 px-4 max-w-screen-lg mx-auto pb-20">
                {articles.map((article: Article) => (
                    <CardComponent
                        key={article.id}
                        article={article}
                        handleAddToCart={handleAddToCart}
                    />
                ))}
            </section>

            {/* Cart Animation */}
            {cartAnimation.id && (
                <div
                    className="cart-animation fixed z-50"
                    style={{ left: cartAnimation.x, top: cartAnimation.y }}
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                        className="w-8 h-8 text-accent"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
                        />
                    </svg>
                </div>
            )}

            {/* Toast Notification */}
            {toast && (
                <div
                    className={`fixed bottom-4 right-4 p-4 rounded-lg shadow-lg text-white ${
                        toast.type === "success" ? "bg-green-500" : "bg-red-500"
                    } animate-toast`}
                >
                    {toast.message}
                </div>
            )}
        </section>
    );
}

export default DisplayProductSection;