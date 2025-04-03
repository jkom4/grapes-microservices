import { useEffect, useState } from "react";
import Fruit from "../../utils/interface/Fruit";
import placeholder from "../../assets/images/fruit.png";
import { useLanguage } from "../../features/LanguageContext";

// PopularSection Component: Fetches and displays a list of popular fruits
function PopularSection() {
    const { language } = useLanguage(); // State to manage language toggle
    const [fruits, setFruits] = useState<Fruit[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // Text content for the section in both languages
    const text = {
        en: {
            header: "Popular Now",
            price: "€ / kg",
            rating: "★",
        },
        fr: {
            header: "Populaire Maintenant",
            price: "€ / kg",
            rating: "★",
        },
    };

    // useEffect hook to fetch fruit data from the API when the component mounts
    useEffect(() => {
        const fetchFruits = async () => {
            try {
                const response = await fetch("http://localhost:3001/fruits");
                if (!response.ok) {
                    throw new Error("Failed to fetch fruits");
                }
                const data: Fruit[] = await response.json();
                const mappedData = data.map((fruit: Fruit) => ({
                    ...fruit,
                    image: placeholder, // Fallback image for fruit items
                }));
                setFruits(mappedData.slice(0, 3)); // Show only the top 3 fruits
                setLoading(false);
            } catch (err) {
                setError(err instanceof Error ? err.message : "An error occurred");
                setLoading(false);
            }
        };

        fetchFruits();
    }, []); // Empty dependency array ensures this runs only once on mount

    // Conditional rendering for loading and error states
    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <section>
            {/* Section header for "Popular Now" */}
            <section className="hero flex justify-center items-center bg-primary py-8">
                <div className="hero-content max-w-screen-lg flex justify-between items-center w-full px-4">
                    <h2 className="text-2xl font-semibold text-black border-b-4 border-accent pb-2 inline-block">
                        {text[language].header}
                    </h2>
                </div>
            </section>

            <section className="relative bg-primary">
                {/* Background for the popular fruits section */}
                <section className="bg-popular absolute left-1/2 transform -translate-x-1/2 w-full max-w-[1268px] h-[330px] bg-secondary rounded-[64px] z-0 top-[100px]"></section>

                {/* Grid layout for displaying the fruit cards */}
                <section className="grid grid-cols-1 md:grid-cols-3 gap-8 px-4 max-w-screen-lg mx-auto relative z-10">
                    {fruits.map((fruit: Fruit) => (
                        <div
                            key={fruit.id}
                            className="card bg-white p-5 w-full max-w-[300px] rounded-lg shadow-lg text-center transition-transform duration-300 ease-in-out hover:translate-y-[-5px] mx-auto"
                        >
                            <div className="relative">
                                {/* Rating badge */}
                                <div className="absolute top-2 left-2">
                                    <span className="bg-white text-secondary text-sm font-semibold px-3 py-1 rounded-full">
                                        {fruit.rating} {text[language].rating}
                                    </span>
                                </div>
                                <img
                                    src={fruit.image}
                                    alt={fruit.name}
                                    className="w-full h-auto rounded-lg mb-4"
                                />
                                {/* Price badge */}
                                <div className="absolute bottom-2 right-2 bg-secondary text-white text-sm font-semibold rounded-full px-3 py-1">
                                    {fruit.price} {text[language].price}
                                </div>
                            </div>
                            <div className="card-header flex justify-between items-center">
                                {/* Fruit name */}
                                <h3 className="text-lg font-semibold text-secondary">
                                    {fruit.name}
                                </h3>
                                {/* Buy button */}
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
        </section>
    );
}

export default PopularSection;
