// src/components/AllArticlesSection.tsx
import React, { useEffect, useState } from "react";
import Article from "../../utils/models/Articles";
import { useLanguage } from "../../features/LanguageContext";
import { fetchFruits } from "../../services/fruitServices";
import searchArticles from "../../services/searchFruitsServices";
import CardComponent from "../../components/CardComponent";
import { cartService } from "../../services/cartService";

function AllArticlesSection({ limit = 0 }: { limit?: number }) {
    const [articles, setArticles] = useState<Article[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState<number>(1);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [searchQuery, setSearchQuery] = useState<string>("");
    const { language } = useLanguage();
    const articlesPerPage = 27;
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
    const [orderId, setOrderId] = useState<string | null>(null); // Nouvel état pour orderId

    const userId = 1;

    const text = {
        en: {
            header: "All articles",
            noArticles: "No articles found matching your search",
            loading: "Loading articles...",
            addToCartSuccess: "Item added to cart!",
            addToCartError: "Failed to add item to cart",
            modalTitle: "Add to Cart",
            quantityKg: "Quantity (kg)",
            quantityUnits: "Quantity (units)",
            unitType: "Unit Type",
            addButton: "Add to Cart",
            cancelButton: "Cancel",
            stockError: "Quantity exceeds available stock!",
        },
        fr: {
            header: "Tous les articles",
            noArticles: "Aucun article trouvé correspondant à votre recherche",
            loading: "Chargement des articles...",
            addToCartSuccess: "Article ajouté au panier !",
            addToCartError: "Échec de l'ajout au panier",
            modalTitle: "Ajouter au panier",
            quantityKg: "Quantité (kg)",
            quantityUnits: "Quantité (unités)",
            unitType: "Type d'unité",
            addButton: "Ajouter au panier",
            cancelButton: "Annuler",
            stockError: "La quantité dépasse le stock disponible !",
        },
    };

    const researchtext = {
        en: { header: "Search an article by name..." },
        fr: { header: "Rechercher un article via le nom..." },
    };

    // Initialiser le panier et charger les articles
    useEffect(() => {
        const initializeCartAndFetchArticles = async () => {
            try {
                setLoading(true);

                // Vérifier si orderId existe dans localStorage
                let dynamicOrderId = localStorage.getItem("orderId");
                if (!dynamicOrderId) {
                    const initResponse = await cartService.initializeCart(userId);
                    dynamicOrderId = initResponse.id.toString();
                    localStorage.setItem("orderId", dynamicOrderId);
                }
                setOrderId(dynamicOrderId);

                // Charger les articles
                const { content } = await fetchFruits(0, limit);
                setArticles(content);
            } catch (err) {
                setError(err instanceof Error ? err.message : "An error occurred");
            } finally {
                setLoading(false);
            }
        };

        initializeCartAndFetchArticles();
    }, [limit, userId]);


    // Sync component state with URL parameters
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

    // Fetch articles based on search query or page change
    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError(null);
            try {
                if (searchQuery === "") {
                    const { content, totalPages } = await fetchFruits(currentPage - 1, articlesPerPage);
                    setArticles(content);
                    setTotalPages(totalPages);
                } else {
                    const { content, totalPages } = await searchArticles(searchQuery, currentPage - 1, articlesPerPage);
                    setArticles(content);
                    setTotalPages(totalPages);
                }
            } catch (err) {
                console.error("Error fetching data:", err);
                setError(err instanceof Error ? err.message : "An error occurred");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [currentPage, searchQuery]);

    const handleOpenModal = (article: Article, event: React.MouseEvent<HTMLButtonElement>) => {
        setSelectedArticle(article);
        setModalOpen(true);
        // Trigger animation
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
            setToast({ message: text[language].addToCartError, type: "error" });
            return;
        }

        const articleId = selectedArticle.id;
        const quantityKgValue = unitType === "kg" ? parseFloat(quantityKg) || 0 : 0;
        const quantityUnitsValue = unitType === "units" ? parseInt(quantityUnits) || 1 : 0;

        // Valider le stock
        if (unitType === "kg" && quantityKgValue > selectedArticle.stockKg) {
            setToast({ message: text[language].stockError, type: "error" });
            return;
        }
        if (unitType === "units" && quantityUnitsValue > selectedArticle.stockUnit) {
            setToast({ message: text[language].stockError, type: "error" });
            return;
        }

        if (quantityKgValue <= 0 && quantityUnitsValue <= 0) {
            setToast({ message: text[language].addToCartError, type: "error" });
            return;
        }

        try {
            await cartService.addItemToCart(
                parseInt(orderId),
                articleId,
                quantityKgValue,
                quantityUnitsValue
            );
            setToast({ message: text[language].addToCartSuccess, type: "success" });

            // Réinitialiser l'animation après un délai
            setTimeout(() => {
                setCartAnimation({ id: null, x: 0, y: 0 });
            }, 1000);

            handleCloseModal();
        } catch (error) {
            console.error("Error adding item to cart:", error);
            setToast({ message: text[language].addToCartError, type: "error" });
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

    // Auto-dismiss toast after 3 seconds
    useEffect(() => {
        if (toast) {
            const timer = setTimeout(() => setToast(null), 3000);
            return () => clearTimeout(timer);
        }
    }, [toast]);

    return (
        <section className="bg-white py-8">
            <section className="hero flex justify-center items-center py-8">
                <div className="hero-content max-w-screen-lg flex justify-between items-center w-full px-4">
                    <h2 className="text-2xl font-semibold text-black border-b-4 border-accent pb-2 inline-block">
                        {text[language].header}
                    </h2>
                </div>
            </section>

            <section className="flex justify-center mb-6 px-4">
                <input
                    type="text"
                    placeholder={researchtext[language].header}
                    value={searchQuery}
                    onChange={handleSearchChange}
                    className="px-4 py-2 border rounded-md w-full sm:w-1/2 md:w-1/3"
                />
            </section>
            {loading && (
                <div className="flex justify-center items-center py-10 text-gray-500 text-lg font-medium">
                    <span className="animate-pulse">{text[language].loading}</span>
                </div>
            )}

            {error && <div className="text-center text-red-500 font-semibold py-4">{error}</div>}

            {!loading && !error && (
                <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8 px-4 max-w-screen-lg mx-auto pb-20">
                    {articles.length > 0 ? (
                        articles.map((article: Article) => (
                            <CardComponent
                                key={article.id}
                                article={article}
                                handleAddToCart={handleOpenModal}
                            />
                        ))
                    ) : (
                        <div className="col-span-full text-center py-8 text-gray-500">
                            <h3 className="text-xl font-semibold">{text[language].noArticles}</h3>
                        </div>
                    )}
                </section>
            )}

            {/* Modal */}
            {modalOpen && selectedArticle && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-lg p-6 w-full max-w-md">
                        <h3 className="text-lg font-semibold mb-4">{text[language].modalTitle}: {selectedArticle.name}</h3>
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700">{text[language].unitType}</label>
                                <select
                                    value={unitType}
                                    onChange={(e) => setUnitType(e.target.value as "kg" | "units")}
                                    className="mt-1 block w-full border border-gray-300 rounded-lg p-2 focus:ring-2 focus:ring-accent"
                                >
                                    <option value="units">{text[language].quantityUnits}</option>
                                    <option value="kg">{text[language].quantityKg}</option>
                                </select>
                            </div>
                            {unitType === "kg" ? (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">
                                        {text[language].quantityKg} (Max: {selectedArticle.stockKg} kg)
                                    </label>
                                    <input
                                        type="number"
                                        value={quantityKg}
                                        onChange={(e) => setQuantityKg(e.target.value)}
                                        min="0"
                                        step="0.1"
                                        max={selectedArticle.stockKg}
                                        className="mt-1 block w-full border border-gray-300 rounded-lg p-2 focus:ring-2 focus:ring-accent"
                                        placeholder="e.g., 1.5"
                                    />
                                </div>
                            ) : (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">
                                        {text[language].quantityUnits} (Max: {selectedArticle.stockUnit} units)
                                    </label>
                                    <input
                                        type="number"
                                        value={quantityUnits}
                                        onChange={(e) => setQuantityUnits(e.target.value)}
                                        min="1"
                                        max={selectedArticle.stockUnit}
                                        className="mt-1 block w-full border border-gray-300 rounded-lg p-2 focus:ring-2 focus:ring-accent"
                                        placeholder="e.g., 1"
                                    />
                                </div>
                            )}
                        </div>
                        <div className="mt-6 flex justify-end space-x-2">
                            <button
                                onClick={handleCloseModal}
                                className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400"
                            >
                                {text[language].cancelButton}
                            </button>
                            <button
                                onClick={handleAddToCart}
                                className="px-4 py-2 bg-secondary text-white rounded-lg hover:bg-accent"
                            >
                                {text[language].addButton}
                            </button>
                        </div>
                    </div>
                </div>
            )}

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

            {!loading && totalPages > 1 && (
                <div className="flex justify-center mt-6">
                    <button
                        onClick={() => handlePageChange(currentPage - 1)}
                        disabled={currentPage === 1}
                        className="px-4 py-2 mx-2 bg-accent text-white rounded-md disabled:opacity-50"
                    >
                        Previous
                    </button>
                    <span className="px-4 py-2 mx-2 text-lg">
            {currentPage} / {totalPages}
          </span>
                    <button
                        onClick={() => handlePageChange(currentPage + 1)}
                        disabled={currentPage === totalPages}
                        className="px-4 py-2 mx-2 bg-accent text-white rounded-md disabled:opacity-50"
                    >
                        Next
                    </button>
                </div>
            )}
        </section>
    );
}

export default AllArticlesSection;