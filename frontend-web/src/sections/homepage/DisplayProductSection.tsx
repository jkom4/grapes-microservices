import React, { useEffect, useState } from "react";
import Fruit from "../../utils/interface/Fruit";
import placeholder from "../../assets/images/fruit.png";
import { useLanguage } from "../../features/LanguageContext";

function DisplayProductSection(props: any) {
    const [fruits, setFruits] = useState<Fruit[]>([]);  // State to store fetched fruits data
    const [loading, setLoading] = useState<boolean>(true);  // Loading state
    const [error, setError] = useState<string | null>(null);  // Error state
    const { language } = useLanguage();  // Access the current language from context

    // Text content in both languages (English and French)
    const text = {
        en: {
            header: "Specially for you",
        },
        fr: {
            header: "Spécialement pour vous",
        }
    };

    useEffect(() => {
        // Function to fetch fruits data from the API
        const fetchFruits = async () => {
            try {
                const response = await fetch("http://localhost:3001/fruits");
                if (!response.ok) {
                    throw new Error("Failed to fetch fruits");
                }
                const data: Fruit[] = await response.json();
                // Map over the data and add a placeholder image to each fruit
                const mappedData = data.map((fruit: Fruit) => ({
                    ...fruit,
                    image: placeholder,
                }));
                setFruits(mappedData.slice(0, 6));  // Set only first 6 fruits
                setLoading(false);
            } catch (err) {
                setError(err instanceof Error ? err.message : "An error occurred");
                setLoading(false);
            }
        };

        fetchFruits();
    }, []);  // Empty dependency array ensures this runs once on component mount

    if (loading) {
        return <div>Loading...</div>;  // Show loading message while data is being fetched
    }

    if (error) {
        return <div>Error: {error}</div>;  // Display error message if there's an issue
    }

    return (
        <section className="bg-white py-8">
            <section className="hero flex justify-center items-center py-8">
                <div className="hero-content max-w-screen-lg flex justify-between items-center w-full px-4">
                    <h2 className="text-2xl font-semibold text-black border-b-4 border-accent pb-2 inline-block">
                        {text[language].header}  {/* Display the header based on the selected language */}
                    </h2>
                </div>
            </section>

            <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8 px-4 max-w-screen-lg mx-auto pb-20">
                {/* Render each fruit as a card */}
                {fruits.map((fruit: Fruit) => (
                    <div
                        key={fruit.id}
                        className="card bg-white p-5 w-full max-w-[300px] rounded-lg shadow-lg text-center transition-transform duration-300 ease-in-out hover:translate-y-[-5px] mx-auto"
                    >
                        <div className="relative">
                            <div className="absolute top-2 left-2">
                                {/* Display the fruit rating */}
                                <span className="bg-white text-secondary text-sm font-semibold px-3 py-1 rounded-full">
                                    {fruit.rating} ★
                                </span>
                            </div>
                            <img
                                src={fruit.image}
                                alt={fruit.name}
                                className="w-full h-auto rounded-lg mb-4"
                            />
                            <div className="absolute bottom-2 right-2 bg-secondary text-white text-sm font-semibold rounded-full px-3 py-1">
                                {/* Display the fruit price */}
                                {fruit.price} € / kg
                            </div>
                        </div>
                        <div className="card-header flex justify-between items-center">
                            <h3 className="text-lg font-semibold text-secondary">
                                {fruit.name}
                            </h3>
                            {/* Button to "buy" the fruit */}
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
