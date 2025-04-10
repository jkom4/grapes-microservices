import { useParams, useNavigate } from "react-router-dom";  // Import useNavigate
import { useEffect, useState } from "react";
import Article from "../../utils/models/Articles";
import { useLanguage } from "../../features/LanguageContext";  // Import the Language Context
import { fetchArticleById } from "../../services/fruitServices";

function ArticleDetailsSection() {
    const { id } = useParams<{ id: string }>();
    const [article, setArticle] = useState<Article | null>(null);
    const [isFavorite, setIsFavorite] = useState(false);

    const { language } = useLanguage();  // Get the current language from context
    const navigate = useNavigate();  // Initialize navigate function from react-router-dom

    // Text content in both languages (English and French)
    const text = {
        en: {
            loading: "Loading...",
            origin: "Origin:",
            stock: "Stock",
            addToCart: "Add to cart",
            back: "Back",  // Text for the back button
        },
        fr: {
            loading: "Chargement...",
            origin: "Origine:",
            stock: "Stock",
            addToCart: "Ajouter au panier",
            back: "Retour",  // Text for the back button
        }
    };

    useEffect(() => {
        const fetchArticle = async () => {
            if (!id) return;
            const articleId = parseInt(id);
            try {
                const articleData = await fetchArticleById(articleId);
                setArticle(articleData);
            } catch (err) {
                console.error("Failed to fetch article:", err);
            }
        };

        fetchArticle();
    }, [id]);

    if (!article) return <div className="text-center mt-10 text-gray-500">{text[language].loading}</div>;

    return (
        <section className="bg-gray-50 py-12 px-6 lg:px-24">
            <div className="flex justify-center items-center max-w-7xl mx-auto">
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center justify-center">
                    <div className="relative flex justify-center">
                        {/* Back button with an icon and text */}
                        <div className="absolute top-0 left-4 flex items-center text-secondary hover:text-accent transition">
                            <button
                                onClick={() => navigate(-1)}  // Go back to the previous page
                                aria-label={text[language].back}
                                className="flex items-center"
                            >
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    className="w-6 h-6 mr-2"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                >
                                    <path d="M19 12H5"></path>
                                    <path d="M12 19l-7-7 7-7"></path>
                                </svg>
                                <span>{text[language].back}</span>  {/* Back text */}
                            </button>
                        </div>

                        {/* Favorite button: toggle the favorite state */}
                        <button
                            onClick={() => setIsFavorite(!isFavorite)} // Change favorite state
                            className={`absolute top-16 right-4 w-12 h-12 rounded-full flex items-center justify-center transition ${
                                isFavorite ? "bg-red-500" : "bg-white border border-gray-300 hover:bg-gray-100"
                            }`}
                            aria-label="Favorite"
                        >
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                className={`w-6 h-6 ${isFavorite ? "text-white" : "text-gray-600"}`}
                                viewBox="0 0 24 24"
                                fill="currentColor"
                            >
                                <path
                                    fillRule="evenodd"
                                    d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                                    clipRule="evenodd"
                                />
                            </svg>
                        </button>

                        {/* Product image */}
                        <img
                            src={article.picturePath}
                            alt={article.name}
                            className="w-full h-auto rounded-xl shadow-lg object-contain mt-12 max-h-[500px]"
                        />
                    </div>

                    {/* Product Info */}
                    <div className="space-y-6 mt-12">
                        <h1 className="text-4xl font-extrabold text-secondary leading-tight">{article.name}</h1>
                        <p className="text-lg text-gray-700">{article.description}</p>

                        <div className="text-md text-gray-600">
                            <span className="font-semibold">{text[language].origin}</span> {article.origin}
                        </div>

                        {/* Price */}
                        <div className="text-2xl font-semibold text-accent">
                            {article.priceKg} € / kg
                            <span className="text-gray-500 text-sm ml-2">({article.priceUnit} € / unit)</span>
                        </div>

                        {/* Stock */}
                        <div className="text-sm text-gray-600">
                            <span className="font-semibold">{text[language].stock}: </span>
                            {article.stockKg} kg — {article.stockUnit} units available
                        </div>

                        {/* Actions */}
                        <div className="flex gap-4 items-center mt-8">
                            {/* Add to cart button */}
                            <button className="bg-accent text-white font-semibold px-8 py-4 rounded-lg shadow-lg transition transform hover:bg-[#D43F97] hover:scale-105">
                                {text[language].addToCart}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}

export default ArticleDetailsSection;
