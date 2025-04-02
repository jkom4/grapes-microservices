import React, { useState, useEffect } from "react";
import choose from "../assets/images/choose.png";
import deliver from "../assets/images/deliver.png";
import enjoy from "../assets/images/enjoy.png";
import fruitImage from "../assets/images/grapes.png";
import placeholder from "../assets/images/fruit.png";
import Fruit from "../utils/interface/Fruit";

// HeaderSection Component: Displays a hero section with text and an image
const HeaderSection: React.FC = () => {
    return (
        <section className="flex justify-center items-center bg-primary py-0">
            {/* Container for content with max width and flex layout */}
            <div className="flex w-full max-w-screen-xl items-center justify-between">
                <div className="hero-text flex-1 text-left">
                    {/* Main heading with styled "fruits" span */}
                    <h1 className="text-5xl font-semibold text-secondary">
                        Enjoy your <span className="text-accent">fruits</span>
                        <br /> before your activity
                    </h1>
                    <p className="mt-4 text-black font-normal">
                        Boost your productivity and build your <br /> mood with a glass of coffee in the morning.
                    </p>
                    {/* Order button with hover effect */}
                    <button className="mt-5 px-6 py-3 bg-secondary text-white rounded-full font-semibold text-sm hover:bg-accent transition-colors duration-300">
                        Order now
                    </button>
                </div>
                <div className="hero-image flex-1 flex justify-end">
                    {/* Hero image displayed on the right */}
                    <img src={fruitImage} alt="Healthy fruit bowl" className="w-3/4" />
                </div>
            </div>
        </section>
    );
};

// PopularSection Component: Fetches and displays a list of popular fruits
function PopularSection() {
    // State for fruits data, loading status, and error handling
    const [fruits, setFruits] = useState<Fruit[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // useEffect to fetch fruit data on component mount
    useEffect(() => {
        const fetchFruits = async () => {
            try {
                // Fetch data from local API endpoint
                const response = await fetch("http://localhost:3001/fruits");
                if (!response.ok) {
                    throw new Error("Failed to fetch fruits");
                }
                const data: Fruit[] = await response.json();
                // Map fetched data to include a placeholder image
                const mappedData = data.map((fruit: Fruit) => ({
                    ...fruit,
                    image: placeholder,
                }));
                setFruits(mappedData);
                setLoading(false);
            } catch (err) {
                // Handle errors and update state
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
                        Popular Now
                    </h2>
                </div>
            </section>

            <section className="relative bg-primary py-10">
                {/* Background decorative element */}
                <section className="bg-popular absolute left-1/2 transform -translate-x-1/2 w-full max-w-[1268px] h-[367px] bg-[#9E14D0] rounded-[64px] z-0 top-[100px]"></section>

                {/* Grid layout for fruit cards */}
                <section className="grid grid-cols-1 md:grid-cols-3 gap-8 px-4 max-w-screen-lg mx-auto relative z-10">
                    {fruits.map((fruit: Fruit) => (
                        <div
                            key={fruit.id} // Unique key for each fruit card
                            className="card bg-white p-5 w-full max-w-[300px] rounded-lg shadow-lg text-center transition-transform duration-300 ease-in-out hover:translate-y-[-5px] mx-auto"
                        >
                            <div className="relative">
                                {/* Rating badge */}
                                <div className="absolute top-2 left-2">
                                    <span className="bg-white text-secondary text-sm font-semibold px-3 py-1 rounded-full">
                                        {fruit.rating} ★
                                    </span>
                                </div>
                                <img
                                    src={fruit.image}
                                    alt={fruit.name}
                                    className="w-full h-auto rounded-lg mb-4"
                                />
                            </div>
                            <div className="card-header flex justify-between items-center">
                                <h3 className="text-lg font-semibold text-secondary">
                                    {fruit.name}
                                </h3>
                                {/* Buy button with SVG icon */}
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

// UseSection Component: Displays a "How to use" section with steps
const UseSection: React.FC = () => {
    // Array of steps with image, text, and subtext
    const steps = [
        {
            src: choose,
            text: "choose your fruits",
            subtext: "there are 20+ coffees for you",
            alt: "Step 1: Choose your fruits"
        },
        {
            src: deliver,
            text: "we delivery it to you",
            subtext: "Choose delivery service",
            alt: "Step 2: We deliver it to you"
        },
        {
            src: enjoy,
            text: "Enjoy your fruits",
            subtext: "Choose delivery service",
            alt: "Step 3: Enjoy your fruits"
        }
    ];

    return (
        <section className="py-12 bg-white">
            <div className="max-w-screen-lg mx-auto px-4">
                <div className="mt-16 mb-8">
                    {/* Section title */}
                    <h2 className="text-2xl font-semibold text-black border-b-4 border-accent pb-5 inline-block pt-7">
                        How to use delivery service
                    </h2>
                </div>

                {/* Flex layout for step items */}
                <div className="flex justify-between items-center">
                    {steps.map((step, index) => (
                        <div key={index} className="flex flex-col items-center">
                            <img
                                src={step.src}
                                alt={step.alt}
                                className="w-32 h-32 object-contain mb-4"
                            />
                            <p className="text-lg font-semibold text-secondary capitalize">
                                {step.text}
                            </p>
                            <p className="text-sm text-gray-500">
                                {step.subtext}
                            </p>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};

// MainPage Component: Combines all sections into the main page
export default function MainPage() {
    return (
        <>
            <HeaderSection />
            <PopularSection />
            <UseSection />
        </>
    );
}