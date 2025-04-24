import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useLanguage } from "../../features/LanguageContext";
import { useAuth } from "../../features/AuthContext"; // Import useAuth
import { useArticleDetails } from "../../hooks/useArticleDetails";
import { cartService } from "../../services/cartService";
import { translationsArticleDetails } from "../../utils/translations-article-details";
import ErrorMessage from "../../utils/models/interface/ErrorMessage";
import LoadingSpinner from "../../utils/models/interface/LoadSpinner";
import ArticleHeader from "../../components/articleDetails/ArticleHeader";
import ArticleImage from "../../components/articleDetails/ArticleImage";
import ArticleInfo from "../../components/articleDetails/ArticleInfo";
import CartAnimation from "../../components/articleDetails/CartAnimation";
import ToastNotification from "../../components/ToastNotifications";

function ArticleDetailsSection() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { language } = useLanguage();
    const { sub } = useAuth(); // Get sub from AuthContext
    const { article, orderId, error, setError } = useArticleDetails(id, sub); // Pass sub to useArticleDetails

    // Log sub for debugging
    console.log("User ID (sub) in ArticleDetailsSection:", sub);

    // State for quantity, measurement type, and UI interactions
    const [isFavorite, setIsFavorite] = useState(false);
    const [quantity, setQuantity] = useState<number>(1);
    const [measurementType, setMeasurementType] = useState<"kg" | "unit">("kg");
    const [cartAnimation, setCartAnimation] = useState<{ id: number | null; x: number; y: number }>({
        id: null,
        x: 0,
        y: 0,
    });
    const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

    // Auto-dismiss toast after 3 seconds
    useEffect(() => {
        if (toast) {
            const timer = setTimeout(() => setToast(null), 3000);
            return () => clearTimeout(timer);
        }
    }, [toast]);

    // Handle adding item to cart
    const handleAddToCart = async (event: React.MouseEvent<HTMLButtonElement>) => {
        if (!article || !orderId) {
            setToast({ message: translationsArticleDetails[language].addToCartError, type: "error" });
            return;
        }

        const articleId = article.id;
        const quantityKg = measurementType === "kg" ? quantity : 0;
        const selectedQuantity = measurementType === "unit" ? quantity : 0;

        // Validate stock
        if (measurementType === "kg" && quantityKg > article.stockKg) {
            setToast({ message: translationsArticleDetails[language].stockError, type: "error" });
            return;
        }
        if (measurementType === "unit" && selectedQuantity > article.stockUnit) {
            setToast({ message: translationsArticleDetails[language].stockError, type: "error" });
            return;
        }
        if (quantityKg <= 0 && selectedQuantity <= 0) {
            setToast({ message: translationsArticleDetails[language].addToCartError, type: "error" });
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
            // Add item to cart
            await cartService.addItemToCart(orderId, articleId, quantityKg, selectedQuantity);
            setToast({ message: translationsArticleDetails[language].addToCartSuccess, type: "success" });

            // Reset animation after a delay
            setTimeout(() => {
                setCartAnimation({ id: null, x: 0, y: 0 });
            }, 1000);
        } catch (error: any) {
            const errorMessage = error.message.includes("Failed to add item to cart")
                ? error.message
                : translationsArticleDetails[language].addToCartError;
            setToast({ message: errorMessage, type: "error" });
            setCartAnimation({ id: null, x: 0, y: 0 });
        }
    };

    // Calculate total price based on measurement type
    const calculateTotalPrice = () => {
        if (!article) return "0.00"; // Return string for consistency
        return measurementType === "kg"
            ? (article.priceKg * quantity).toFixed(2)
            : (article.priceUnit * quantity).toFixed(2);
    };

    // Handle quantity change
    const handleQuantityChange = (delta: number) => {
        if (!article) return;
        const maxQuantity = measurementType === "kg" ? article.stockKg : article.stockUnit;
        const newQuantity = Math.max(1, Math.min(quantity + delta, maxQuantity));
        setQuantity(newQuantity);
    };

    // Handle direct quantity input
    const handleQuantityInput = (value: number) => {
        if (!article || isNaN(value)) return;
        const maxQuantity = measurementType === "kg" ? article.stockKg : article.stockUnit;
        setQuantity(Math.max(1, Math.min(value, maxQuantity)));
    };

    if (error) {
        return <ErrorMessage message={error} />;
    }

    if (!article) {
        return <LoadingSpinner message={translationsArticleDetails[language].loading} />;
    }

    return (
        <section className="bg-gray-50 py-12 px-6 lg:px-24">
            <div className="flex justify-center items-center max-w-7xl mx-auto">
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center justify-center">
                    <div className="relative flex justify-center">
                        <ArticleHeader
                            isFavorite={isFavorite}
                            setIsFavorite={setIsFavorite}
                            onBack={() => navigate(-1)}
                            translations={translationsArticleDetails[language]}
                        />
                        <ArticleImage article={article} />
                    </div>
                    <ArticleInfo
                        article={article}
                        totalPrice={calculateTotalPrice()}
                        measurementType={measurementType}
                        quantity={quantity}
                        setMeasurementType={setMeasurementType}
                        onQuantityChange={handleQuantityChange}
                        onQuantityInput={handleQuantityInput}
                        onAddToCart={handleAddToCart}
                        orderId={orderId}
                        translations={translationsArticleDetails[language]}
                    />
                </div>
            </div>
            <CartAnimation cartAnimation={cartAnimation} />
            <ToastNotification toast={toast} />
        </section>
    );
}

export default ArticleDetailsSection;