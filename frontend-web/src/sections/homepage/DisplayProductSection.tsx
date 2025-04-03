// DisplayProductSection.tsx
import React, { useEffect, useState } from "react";
import Article from "../../utils/models/Articles";
import placeholder from "../../assets/images/fruit.png";
import { useLanguage } from "../../features/LanguageContext";
import fetchFruits from "../../services/fruitServices";

function DisplayProductSection({ limit = 6 }: { limit?: number })  {
    const [articles, setArticles] = useState<Article[]>([]); // Typing with the Article class
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const { language } = useLanguage();

    const text = {
        en: {
            header: "Specially for you",
        },
        fr: {
            header: "Spécialement pour vous",
        },
    };

    useEffect(() => {
        const fetchData = async () => {
            try {
                const data = await fetchFruits(limit);
                setArticles(data);
                setLoading(false);
            } catch (err) {
                setError(err instanceof Error ? err.message : "An error occurred");
                setLoading(false);
            }
        };

        fetchData();
    }, [limit]);

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
                    <div
                        key={article.id} // Using id as the unique key
                        className="card bg-white p-5 w-full max-w-[300px] rounded-lg shadow-lg text-center transition-transform duration-300 ease-in-out hover:translate-y-[-5px] mx-auto"
                    >
                        <div className="relative">
                            <div className="absolute top-2 left-2">
                                <span className="bg-white text-secondary text-sm font-semibold px-3 py-1 rounded-full">
                                    {article.rating} ★
                                </span>
                            </div>
                            <img
                                src={article.picturePath}
                                alt={article.name}
                                className="w-full h-auto rounded-lg mb-4"
                            />
                            <div className="absolute bottom-2 right-2 bg-secondary text-white text-sm font-semibold rounded-full px-3 py-1">
                                {article.priceKg} € / kg
                            </div>
                        </div>
                        <div className="card-header flex justify-between items-center">
                            <h3 className="text-lg font-semibold text-secondary">
                                {article.name}
                            </h3>
                            <button className="buy-btn bg-accent text-white w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm cursor-pointer hover:bg-[#D43F97]">
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    stroke="currentColor"
                                    className="w-5 h-5"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth="2"
                                        d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
                                    />
                                </svg>
                            </button>
                        </div>
                    </div>
                ))}
            </section>
        </section>
    );
}

export default DisplayProductSection;
