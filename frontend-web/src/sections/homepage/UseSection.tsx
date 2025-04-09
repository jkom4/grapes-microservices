import { useState } from "react";
import choose from "../../assets/images/choose.png";
import deliver from "../../assets/images/deliver.png";
import enjoy from "../../assets/images/enjoy.png";
import { useLanguage } from "../../features/LanguageContext";

// UseSection Component: Displays a "How to use" section with steps
const UseSection: React.FC = () => {
    const { language } = useLanguage(); // State to manage language toggle

    // Text content in both languages
    const text = {
        en: {
            header: "How to use delivery service", // Header for the section
            steps: [
                {
                    text: "Choose your fruits", // Step 1 title
                    subtext: "There are 100+ fruits for you", // Step 1 description
                },
                {
                    text: "We deliver it to you", // Step 2 title
                    subtext: "Choose delivery service", // Step 2 description
                },
                {
                    text: "Enjoy your fruits", // Step 3 title
                    subtext: "Choose delivery service", // Step 3 description
                },
            ],
        },
        fr: {
            header: "Comment utiliser le service de livraison", // Header for the section in French
            steps: [
                {
                    text: "Choisissez vos fruits", // Step 1 title in French
                    subtext: "Il y a plus de 100 fruits pour vous", // Step 1 description in French
                },
                {
                    text: "Nous vous le livrons", // Step 2 title in French
                    subtext: "Choisissez le service de livraison", // Step 2 description in French
                },
                {
                    text: "Savourez vos fruits", // Step 3 title in French
                    subtext: "Choisissez le service de livraison", // Step 3 description in French
                },
            ],
        },
    };

    return (
        <section className="py-12 bg-white">
            <div className="max-w-screen-lg mx-auto px-4">
                <div className="mt-16 mb-8">
                    {/* Section title */}
                    <h2 className="text-2xl font-semibold text-black border-b-4 border-accent pb-2 inline-block pt-16">
                        {text[language].header} {/* Display header based on selected language */}
                    </h2>
                </div>

                {/* Flex layout for step items */}
                <div className="flex justify-between items-center pt-10 pb-10">
                    {text[language].steps.map((step, index) => (
                        <div key={index} className="flex flex-col items-center">
                            {/* Display images for each step */}
                            <img
                                src={[choose, deliver, enjoy][index]} // Use respective images for each step
                                alt={`Step ${index + 1}: ${step.text}`} // Dynamic alt text based on the step
                                className="w-32 h-32 object-contain mb-4" // Style for the image
                            />
                            {/* Step title */}
                            <p className="text-lg font-semibold text-secondary capitalize">
                                {step.text}
                            </p>
                            {/* Step description */}
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

export default UseSection;
