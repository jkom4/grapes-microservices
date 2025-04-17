import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import Article from "../../utils/models/Articles";
import { useLanguage } from "../../features/LanguageContext";
import { fetchArticleById } from "../../services/fruitServices";
import { cartService } from "../../services/cartService";

function ArticleDetailsSection() {
    const { id } = useParams<{ id: string }>();
    const [article, setArticle] = useState<Article | null>(null);
    const [isFavorite, setIsFavorite] = useState(false);
    const { language } = useLanguage();
    const navigate = useNavigate();

    // State for quantity, measurement type, and animations
    const [quantity, setQuantity] = useState<number>(1);
    const [measurementType, setMeasurementType] = useState<"kg" | "unit">("kg");
    const [cartAnimation, setCartAnimation] = useState<{ id: number | null; x: number; y: number }>({
        id: null,
        x: 0,
        y: 0,
    });
    const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

    const text = {
        en: {
            loading: "Loading...",
            origin: "Origin:",
            stock: "Quantity",
            addToCart: "Add to cart",
            back: "Back",
            addToCartSuccess: "Item added to cart!",
            addToCartError: "Failed to add item to cart",
            kg: "kg",
            unit: "unit",
        },
        fr: {
            loading: "Chargement...",
            origin: "Origine:",
            stock: "Quantité",
            addToCart: "Ajouter au panier",
            back: "Retour",
            addToCartSuccess: "Article ajouté au panier !",
            addToCartError: "Échec de l'ajout au panier",
            kg: "kg",
            unit: "unité",
        },
    };

    useEffect(() => {
        const fetchArticle = async () => {
            if (!id) return;
            const articleId = parseInt(id);
            try {
                console.log("Fetching article with ID:", articleId);
                const articleData = await fetchArticleById(articleId);
                console.log("Article fetched successfully:", articleData);
                setArticle(articleData);
            } catch (err) {
                console.error("Failed to fetch article:", err);
            }
        };

        fetchArticle();
    }, [id]);

    // Auto-dismiss toast after 3 seconds
    useEffect(() => {
        if (toast) {
            const timer = setTimeout(() => setToast(null), 3000);
            return () => clearTimeout(timer);
        }
    }, [toast]);

    const handleAddToCart = async (event: React.MouseEvent<HTMLButtonElement>) => {
        if (!article) return;

        console.log("Attempting to add item to the cart...");

        const orderId = 1; // Replace with actual order ID logic
        const articleId = article.id;
        const quantityKg = measurementType === "kg" ? quantity : 0;
        const selectedQuantity = measurementType === "unit" ? quantity : 0;

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
            // Use cartService to add item to cart
            const response = await cartService.addItemToCart(
                orderId,
                articleId,
                quantityKg,
                selectedQuantity
            );

            console.log("Item added to cart successfully:", response);
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

    // Calculate total price based on measurement type
    const calculateTotalPrice = () => {
        if (!article) return 0;
        return measurementType === "kg"
            ? (article.priceKg * quantity).toFixed(2)
            : (article.priceUnit * quantity).toFixed(2);
    };

    // Handle quantity increment/decrement
    const handleQuantityChange = (delta: number) => {
        if (!article) return;
        const maxQuantity = measurementType === "kg" ? article.stockKg : article.stockUnit;
        const newQuantity = Math.max(1, Math.min(quantity + delta, maxQuantity));
        setQuantity(newQuantity);
    };

    if (!article) {
        return <div className="text-center mt-10 text-gray-500">{text[language].loading}</div>;
    }

    return (
        <section className="bg-gray-50 py-12 px-6 lg:px-24">
            <div className="flex justify-center items-center max-w-7xl mx-auto">
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center justify-center">
                    <div className="relative flex justify-center">
                        {/* Back button */}
                        <div className="absolute top-0 left-4 flex items-center text-secondary hover:text-accent transition">
                            <button
                                onClick={() => navigate(-1)}
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
                                <span>{text[language].back}</span>
                            </button>
                        </div>

                        {/* Favorite button */}
                        <button
                            onClick={() => setIsFavorite(!isFavorite)}
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

                    {/* Product info */}
                    <div className="space-y-6 mt-12">
                        <h1 className="text-4xl font-extrabold text-secondary leading-tight">{article.name}</h1>
                        <p className="text-lg text-gray-700">{article.description}</p>

                        <div className="text-md text-gray-600">
                            <span className="font-semibold">{text[language].origin}</span> {article.origin}
                        </div>

                        {/* Price */}
                        <div className="text-2xl font-semibold text-accent transition-all duration-300">
                            {calculateTotalPrice()} €{" "}
                            <span className="text-gray-500 text-base ml-2">
                ({measurementType === "kg" ? `${article.priceKg} € / kg` : `${article.priceUnit} € / unit`})
              </span>
                        </div>

                        {/* Stock */}
                        <div className="text-sm text-gray-600">
                            <span className="font-semibold">{text[language].stock}: </span>
                            {article.stockKg} kg — {article.stockUnit} units available
                        </div>

                        {/* Measurement type selector */}
                        <div className="mt-4">
                            <label className="text-sm font-semibold text-gray-700 mb-2 block">
                                {text[language].stock}
                            </label>
                            <div className="flex space-x-2">
                                <button
                                    onClick={() => {
                                        setMeasurementType("kg");
                                        setQuantity(1);
                                    }}
                                    className={`px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 ${
                                        measurementType === "kg"
                                            ? "bg-accent text-white"
                                            : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                                    }`}
                                >
                                    {text[language].kg}
                                </button>
                                <button
                                    onClick={() => {
                                        setMeasurementType("unit");
                                        setQuantity(1);
                                    }}
                                    className={`px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 ${
                                        measurementType === "unit"
                                            ? "bg-accent text-white"
                                            : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                                    }`}
                                >
                                    {text[language].unit}
                                </button>
                            </div>
                        </div>

                        {/* Quantity selector */}
                        <div className="flex items-center mt-4">
                            <div className="flex items-center border border-gray-300 rounded-lg overflow-hidden">
                                <button
                                    onClick={() => handleQuantityChange(-1)}
                                    disabled={quantity <= 1}
                                    className="px-4 py-2 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                                >
                                    -
                                </button>
                                <input
                                    type="number"
                                    value={quantity}
                                    onChange={(e) => {
                                        const value = parseInt(e.target.value);
                                        if (!isNaN(value)) {
                                            const maxQuantity = measurementType === "kg" ? article.stockKg : article.stockUnit;
                                            setQuantity(Math.max(1, Math.min(value, maxQuantity)));
                                        }
                                    }}
                                    className="w-16 h-10 text-center border-none focus:ring-0 appearance-none"
                                />
                                <button
                                    onClick={() => handleQuantityChange(1)}
                                    disabled={quantity >= (measurementType === "kg" ? article.stockKg : article.stockUnit)}
                                    className="px-4 py-2 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                                >
                                    +
                                </button>
                            </div>
                            <span className="ml-2 text-sm text-gray-600">
                {measurementType === "kg" ? text[language].kg : text[language].unit}
              </span>
                        </div>

                        {/* Add to cart button */}
                        <div className="flex gap-4 items-center mt-8">
                            <button
                                onClick={handleAddToCart}
                                className="bg-accent text-white font-semibold px-8 py-4 rounded-lg shadow-lg transition transform hover:bg-[#D43F97] hover:scale-105 relative overflow-hidden"
                            >
                                {text[language].addToCart}
                                <span className="pulse-effect absolute inset-0 rounded-lg bg-white opacity-0"></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

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

export default ArticleDetailsSection;