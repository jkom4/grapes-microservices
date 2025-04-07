// src/pages/ArticleDetails.tsx
import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Article from "../../utils/models/Articles";
import fetchFruits from "../../services/fruitServices";
import { useLanguage } from "../../features/LanguageContext";

function ArticleDetails() {
    const { id } = useParams<{ id: string }>();
    const [article, setArticle] = useState<Article | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();
    const { language } = useLanguage();

    const text = {
        en: {
            description: "Description",
            price: "Price",
            quantity: "Available Quantity",
            back: "Back to Products",
        },
        fr: {
            description: "Description",
            price: "Prix",
            quantity: "Quantité disponible",
            back: "Retour aux produits",
        },
    };

    useEffect(() => {
        const fetchProduct = async () => {
            try {
                const data = await fetchFruits();
                const found = data.find((item) => item.id === Number(id));
                if (!found) throw new Error("Product not found");
                setArticle(found);
            } catch (err) {
                setError(err instanceof Error ? err.message : "An error occurred");
            } finally {
                setLoading(false);
            }
        };
        fetchProduct();
    }, [id]);

    if (loading) return <div className="text-center py-20">Loading...</div>;
    if (error) return <div className="text-center py-20 text-red-500">Error: {error}</div>;
    if (!article) return <div className="text-center py-20">Product not found</div>;

    return (
        <section className="bg-white py-12 min-h-screen flex items-center">
            <div className="max-w-screen-lg mx-auto px-4">
                <h2 className="text-3xl font-semibold text-secondary mb-6">{article.name}</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <img src={article.picturePath} alt={article.name} className="rounded-lg shadow-lg" />
                    <div className="space-y-4">
                        <p><strong>{text[language].description}:</strong> {article.description}</p>
                        <p><strong>{text[language].price}:</strong> {article.priceKg} € / kg</p>
                        <p><strong>{text[language].quantity}:</strong> {article.stockKg} kg</p>
                        <button
                            onClick={() => navigate(-1)}
                            className="mt-4 bg-accent text-white px-6 py-2 rounded-lg hover:bg-[#D43F97] transition-colors"
                        >
                            {text[language].back}
                        </button>
                    </div>
                </div>
            </div>
        </section>
    );
}

export default ArticleDetails;
