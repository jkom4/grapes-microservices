import React, { useEffect, useState } from "react";
import Article from "../../utils/models/Articles";
import placeholder from "../../assets/images/fruit.png";
import { useLanguage } from "../../features/LanguageContext";
import fetchFruits from "../../services/fruitServices";
import searchArticles from "../../services/searchFruitsServices";

function AllArticlesSection() {
    const [articles, setArticles] = useState<Article[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState<number>(1);
    const [totalArticles, setTotalArticles] = useState<number>(0);
    const [searchQuery, setSearchQuery] = useState<string>("");  // Search query state
    const { language } = useLanguage();

    const articlesPerPage = 27;

    const text = {
        en: {
            header: "All articles",
            noArticles: "No articles found matching your search",
        },
        fr: {
            header: "Tous les articles",
            noArticles: "Aucun article trouvé correspondant à votre recherche",
        },
    };

    const researchtext = {
        en: {
            header: "Search an article by name...",
        },
        fr: {
            header: "Rechercher un article via le nom...",
        },
    };

    // Fetch all articles initially
    useEffect(() => {
        const fetchData = async () => {
            try {
                const data = await fetchFruits(0);  // Fetch all articles initially
                setArticles(data);
                setTotalArticles(data.length);
                setLoading(false);
            } catch (err) {
                setError(err instanceof Error ? err.message : "An error occurred");
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    // Fetch search results when searchQuery changes
    useEffect(() => {
        if (searchQuery === "") {
            // If search query is empty, fetch all articles again
            const fetchData = async () => {
                try {
                    const data = await fetchFruits(0);
                    setArticles(data);
                    setTotalArticles(data.length);
                    setLoading(false);
                } catch (err) {
                    setError(err instanceof Error ? err.message : "An error occurred");
                    setLoading(false);
                }
            };
            fetchData();
        } else {
            const fetchSearchResults = async () => {
                try {
                    const data = await searchArticles(searchQuery); // Use the search service
                    setArticles(data);
                    setTotalArticles(data.length);
                    setLoading(false);
                } catch (err) {
                    setError("Failed to fetch search results");
                    setLoading(false);
                }
            };
            fetchSearchResults();
        }
    }, [searchQuery]);

    const totalPages = Math.ceil(totalArticles / articlesPerPage);

    const handlePageChange = (page: number) => {
        if (page >= 1 && page <= totalPages) {
            setCurrentPage(page);
        }
    };

    const currentArticles = articles.slice(
        (currentPage - 1) * articlesPerPage,
        currentPage * articlesPerPage
    );

    return (
        <section className="bg-white py-8">
            <section className="hero flex justify-center items-center py-8">
                <div className="hero-content max-w-screen-lg flex justify-between items-center w-full px-4">
                    <h2 className="text-2xl font-semibold text-black border-b-4 border-accent pb-2 inline-block">
                        {text[language].header}
                    </h2>
                </div>
            </section>

            {/* Search bar */}
            <section className="flex justify-center mb-6">
                <input
                    type="text"
                    placeholder={researchtext[language].header}
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}  // Update search query
                    className="px-4 py-2 border rounded-md w-1/3"
                />
            </section>

            <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8 px-4 max-w-screen-lg mx-auto pb-20">
                {currentArticles.length > 0 ? (
                    currentArticles.map((article: Article) => (
                        <div
                            key={article.id}
                            className="card bg-white p-5 w-full max-w-[300px] rounded-lg shadow-lg text-center transition-transform duration-300 ease-in-out hover:translate-y-[-5px] mx-auto"
                        >
                            <div className="relative">
                                <div className="absolute top-2 left-2">
                                    <span className="bg-white text-secondary text-sm font-semibold px-3 py-1 rounded-full">
                                        {article.rating} ★
                                    </span>
                                </div>
                                <img
                                    src={article.picturePath || placeholder} // Fallback image
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
                    ))
                ) : (
                    <div className="col-span-full text-center py-8">
                        <h3 className="text-xl font-semibold text-gray-500">
                            {text[language].noArticles}
                        </h3>
                    </div>
                )}
            </section>

            {/* Pagination controls */}
            <div className="flex justify-center mt-6">
                <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 1}
                    className="px-4 py-2 mx-2 bg-accent text-white rounded-md"
                >
                    Previous
                </button>
                <span className="px-4 py-2 mx-2 text-lg">{currentPage} / {totalPages}</span>
                <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage === totalPages}
                    className="px-4 py-2 mx-2 bg-accent text-white rounded-md"
                >
                    Next
                </button>
            </div>
        </section>
    );
}

export default AllArticlesSection;
