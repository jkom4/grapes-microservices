import React, { useEffect, useState } from "react";
import Article from "../../utils/models/Articles";
import placeholder from "../../assets/images/fruit.png";
import { useLanguage } from "../../features/LanguageContext";
import {fetchFruits} from "../../services/fruitServices";
import searchArticles from "../../services/searchFruitsServices";
import {useNavigate} from "react-router-dom";

// Component to display a paginated list of articles with search functionality
function AllArticlesSection() {
    const [articles, setArticles] = useState<Article[]>([]); // List of articles to display
    const [loading, setLoading] = useState<boolean>(true); // Loading state for data fetching
    const [error, setError] = useState<string | null>(null); // Error message if fetching fails
    const [currentPage, setCurrentPage] = useState<number>(1); // Current page number
    const [totalPages, setTotalPages] = useState<number>(0); // Total number of pages
    const [searchQuery, setSearchQuery] = useState<string>(""); // Search query input
    const { language } = useLanguage(); // Language context for multilingual support

    const articlesPerPage = 27; // Number of articles per page
    const navigate = useNavigate();  // Initialize navigate function from react-router-dom

    // Text content for different languages (English and French)
    const text = {
        en: { header: "All articles", noArticles: "No articles found matching your search", loading: "Loading articles..." },
        fr: { header: "Tous les articles", noArticles: "Aucun article trouvé correspondant à votre recherche", loading: "Chargement des articles..." },
    };

    const researchtext = {
        en: { header: "Search an article by name..." },
        fr: { header: "Rechercher un article via le nom..." },
    };

    // Sync component state with URL parameters (search query and page)
    const syncStateWithURL = () => {
        const params = new URLSearchParams(window.location.search);
        const query = params.get("search") || ""; // Get search query from URL
        const page = parseInt(params.get("page") || "1", 10); // Get page number from URL
        setSearchQuery(query);
        setCurrentPage(page);
    };

    // Initial sync with URL on component mount
    useEffect(() => {
        syncStateWithURL();
    }, []);

    // Add and remove event listener for browser back/forward navigation
    useEffect(() => {
        window.addEventListener("popstate", syncStateWithURL);
        return () => window.removeEventListener("popstate", syncStateWithURL); // Cleanup
    }, []);

    // Fetch articles based on search query or page change
    useEffect(() => {
        const fetchData = async () => {
            setLoading(true); // Start loading
            setError(null); // Clear previous errors
            try {
                if (searchQuery === "") {
                    // Fetch all available articles when no search query is provided
                    const { content, totalPages } = await fetchFruits(currentPage - 1, articlesPerPage);
                    setArticles(content);
                    setTotalPages(totalPages);
                } else {
                    // Fetch articles based on search query
                    const { content, totalPages } = await searchArticles(searchQuery, currentPage - 1, articlesPerPage);
                    setArticles(content);
                    setTotalPages(totalPages);
                }
            } catch (err) {
                console.error("Error fetching data:", err);
                setError(err instanceof Error ? err.message : "An error occurred"); // Set error message
            } finally {
                setLoading(false); // End loading
            }
        };

        fetchData();
    }, [currentPage, searchQuery]); // Re-run when page or search query changes

    // Handle pagination by updating the current page and URL
    const handlePageChange = (page: number) => {
        if (page >= 1 && page <= totalPages) {
            setCurrentPage(page);
            // Update URL without reloading the page
            window.history.pushState(
                {},
                "",
                searchQuery === "" ? `?page=${page}` : `?search=${encodeURIComponent(searchQuery)}&page=${page}`
            );
        }
    };

    // Handle search input change, reset to page 1, and update URL
    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newQuery = e.target.value;
        setSearchQuery(newQuery);
        setCurrentPage(1); // Reset to first page on new search
        window.history.pushState({}, "", newQuery === "" ? `?page=1` : `?search=${encodeURIComponent(newQuery)}&page=1`);
    };

    // Render the UI
    return (
        <section className="bg-white py-8">
            {/* Header Section */}
            <section className="hero flex justify-center items-center py-8">
                <div className="hero-content max-w-screen-lg flex justify-between items-center w-full px-4">
                    <h2 className="text-2xl font-semibold text-black border-b-4 border-accent pb-2 inline-block">
                        {text[language].header} {/* Display header based on language */}
                    </h2>
                </div>
            </section>

            {/* Search Bar */}
            <section className="flex justify-center mb-6 px-4">
                <input
                    type="text"
                    placeholder={researchtext[language].header} // Placeholder based on language
                    value={searchQuery}
                    onChange={handleSearchChange}
                    className="px-4 py-2 border rounded-md w-full sm:w-1/2 md:w-1/3"
                />
            </section>

            {/* Loading Indicator */}
            {loading && (
                <div className="flex justify-center items-center py-10 text-gray-500 text-lg font-medium">
                    <span className="animate-pulse">{text[language].loading}</span>
                </div>
            )}

            {/* Error Message */}
            {error && <div className="text-center text-red-500 font-semibold py-4">{error}</div>}

            {/* Articles Grid */}
            {!loading && !error && (
                <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8 px-4 max-w-screen-lg mx-auto pb-20">
                    {articles.length > 0 ? (
                        articles.map((article: Article) => (
                            <div
                                key={article.id}
                                className="card bg-white p-5 w-full max-w-[300px] rounded-lg shadow-lg text-center transition-transform duration-300 ease-in-out hover:translate-y-[-5px] mx-auto"
                                onClick={() => navigate(`/clm/articles/clm/articles/${article.id}`)}
                            >
                                <div className="relative">
                                    <div className="absolute top-2 left-2">
                                        <span className="bg-white text-secondary text-sm font-semibold px-3 py-1 rounded-full">
                                            {article.rating} ★ {/* Article rating */}
                                        </span>
                                    </div>
                                    <img
                                        src={article.picturePath || placeholder} // Use placeholder if no image
                                        alt={article.name}
                                        className="w-full h-auto rounded-lg mb-4"
                                    />
                                    <div className="absolute bottom-2 right-2 bg-secondary text-white text-sm font-semibold rounded-full px-3 py-1">
                                        {article.priceKg} € / kg {/* Price per kg */}
                                    </div>
                                </div>
                                <div className="card-header flex justify-between items-center">
                                    <h3 className="text-lg font-semibold text-secondary">{article.name}</h3>
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
                        <div className="col-span-full text-center py-8 text-gray-500">
                            <h3 className="text-xl font-semibold">{text[language].noArticles}</h3> {/* No articles message */}
                        </div>
                    )}
                </section>
            )}

            {/* Pagination Controls */}
            {!loading && totalPages > 1 && (
                <div className="flex justify-center mt-6">
                    <button
                        onClick={() => handlePageChange(currentPage - 1)}
                        disabled={currentPage === 1} // Disable if on first page
                        className="px-4 py-2 mx-2 bg-accent text-white rounded-md disabled:opacity-50"
                    >
                        Previous
                    </button>
                    <span className="px-4 py-2 mx-2 text-lg">
                        {currentPage} / {totalPages} {/* Display current page and total */}
                    </span>
                    <button
                        onClick={() => handlePageChange(currentPage + 1)}
                        disabled={currentPage === totalPages} // Disable if on last page
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