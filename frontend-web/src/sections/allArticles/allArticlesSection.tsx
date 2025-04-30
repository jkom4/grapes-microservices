import React, { useEffect, useState, useMemo } from "react";
import Article from "../../utils/models/Articles";
import { useLanguage } from "../../features/LanguageContext";
import { cartService } from "../../services/cartService";
import { translationsAllArticles } from "../../utils/translations-all-articles";
import ArticleGrid from "../../components/allArticles/ArticleGrid";
import SearchBar from "../../components/allArticles/SearchBar";
import LoadingSpinner from "../../utils/models/interface/LoadSpinner";
import ErrorMessage from "../../utils/models/interface/ErrorMessage";
import ArticleModal from "../../components/allArticles/ArticleModal";
import CartAnimation from "../../components/allArticles/CartAnimation";
import ToastNotification from "../../components/ToastNotifications";
import Pagination from "../../components/allArticles/Pagination";
import { useCart } from "../../features/CartContext";
import useArticles from "../../hooks/useArticle";

function AllArticlesSection({ limit = 0 }: { limit?: number }) {
    const [currentPage, setCurrentPage] = useState<number>(1);
    const [searchQuery, setSearchQuery] = useState<string>("");
    const [cartAnimation, setCartAnimation] = useState<{ id: number | null; x: number; y: number }>({
        id: null,
        x: 0,
        y: 0,
    });
    const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);
    const [modalOpen, setModalOpen] = useState<boolean>(false);
    const [selectedArticle, setSelectedArticle] = useState<Article | null>(null);
    const [quantityKg, setQuantityKg] = useState<string>("0");
    const [quantityUnits, setQuantityUnits] = useState<string>("1");
    const [unitType, setUnitType] = useState<"kg" | "units">("units");
    const { orderId } = useCart();
    const { language } = useLanguage();
    const articlesPerPage = 27;

    const { articles, loading, error, totalPages } = useArticles(currentPage, searchQuery, articlesPerPage, limit);

    const syncStateWithURL = () => {
        const params = new URLSearchParams(window.location.search);
        const query = params.get("search") || "";
        const page = parseInt(params.get("page") || "1", 10);
        setSearchQuery(query);
        setCurrentPage(page);
    };

    useEffect(() => {
        syncStateWithURL();
    }, []);

    useEffect(() => {
        window.addEventListener("popstate", syncStateWithURL);
        return () => window.removeEventListener("popstate", syncStateWithURL);
    }, []);

    const handleOpenModal = (article: Article, event: React.MouseEvent<HTMLButtonElement>) => {
        setSelectedArticle(article);
        setModalOpen(true);
        const button = event.currentTarget;
        const rect = button.getBoundingClientRect();
        setCartAnimation({
            id: article.id,
            x: rect.left + rect.width / 2,
            y: rect.top + rect.height / 2,
        });
    };

    const handleCloseModal = () => {
        setModalOpen(false);
        setSelectedArticle(null);
        setQuantityKg("0");
        setQuantityUnits("1");
        setUnitType("units");
        setCartAnimation({ id: null, x: 0, y: 0 });
    };

    const handleAddToCart = async () => {
        if (!selectedArticle || !orderId) {
            setToast({ message: translationsAllArticles[language].addToCartError, type: "error" });
            return;
        }

        const articleId = selectedArticle.id;
        const quantityKgValue = unitType === "kg" ? parseFloat(quantityKg) || 0 : 0;
        const quantityUnitsValue = unitType === "units" ? parseInt(quantityUnits) || 1 : 0;

        if (unitType === "kg" && quantityKgValue > selectedArticle.stockKg) {
            setToast({ message: translationsAllArticles[language].stockError, type: "error" });
            return;
        }
        if (unitType === "units" && quantityUnitsValue > selectedArticle.stockUnit) {
            setToast({ message: translationsAllArticles[language].stockError, type: "error" });
            return;
        }
        if (quantityKgValue <= 0 && quantityUnitsValue <= 0) {
            setToast({ message: translationsAllArticles[language].addToCartError, type: "error" });
            return;
        }

        try {
            await cartService.addItemToCart(orderId, articleId, quantityKgValue, quantityUnitsValue);
            setToast({ message: translationsAllArticles[language].addToCartSuccess, type: "success" });

            setTimeout(() => {
                setCartAnimation({ id: null, x: 0, y: 0 });
            }, 1000);

            handleCloseModal();
        } catch (error) {
            console.error("Error adding item to cart:", error);
            setToast({ message: translationsAllArticles[language].addToCartError, type: "error" });
            setCartAnimation({ id: null, x: 0, y: 0 });
        }
    };

    const handlePageChange = (page: number) => {
        if (page >= 1 && page <= totalPages) {
            setCurrentPage(page);
            window.history.pushState(
                {},
                "",
                searchQuery === "" ? `?page=${page}` : `?search=${encodeURIComponent(searchQuery)}&page=${page}`
            );
        }
    };

    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newQuery = e.target.value;
        setSearchQuery(newQuery);
        setCurrentPage(1);
        window.history.pushState({}, "", newQuery === "" ? `?page=1` : `?search=${encodeURIComponent(newQuery)}&page=1`);
    };

    useEffect(() => {
        if (toast) {
            const timer = setTimeout(() => setToast(null), 3000);
            return () => clearTimeout(timer);
        }
    }, [toast]);

    const articleGrid = useMemo(
        () => (
            <ArticleGrid
                articles={articles}
                translations={translationsAllArticles[language]}
                handleOpenModal={handleOpenModal}
            />
        ),
        [articles, language, handleOpenModal]
    );

    return (
        <section className="bg-white py-8">
            <section className="hero flex justify-center items-center py-8">
                <div className="hero-content max-w-screen-lg flex justify-between items-center w-full px-4">
                    <h2 className="text-2xl font-semibold text-black border-b-4 border-accent pb-2 inline-block">
                        {translationsAllArticles[language].header}
                    </h2>
                </div>
            </section>

            <SearchBar
                searchQuery={searchQuery}
                onSearchChange={handleSearchChange}
                placeholder={translationsAllArticles[language].searchPlaceholder}
            />

            {loading && <LoadingSpinner message={translationsAllArticles[language].loading} />}
            {error && <ErrorMessage message={error} />}
            {!loading && !error && articleGrid}

            <ArticleModal
                isOpen={modalOpen}
                article={selectedArticle}
                unitType={unitType}
                setUnitType={setUnitType}
                quantityKg={quantityKg}
                setQuantityKg={setQuantityKg}
                quantityUnits={quantityUnits}
                setQuantityUnits={setQuantityUnits}
                onClose={handleCloseModal}
                onAddToCart={handleAddToCart}
                translations={translationsAllArticles[language]}
            />

            <CartAnimation cartAnimation={cartAnimation} />
            <ToastNotification toast={toast} />

            {!loading && totalPages > 1 && (
                <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                />
            )}
        </section>
    );
}

export default AllArticlesSection;