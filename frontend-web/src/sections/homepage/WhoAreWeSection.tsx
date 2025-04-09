import React, { useState } from "react";
import cameronPicture from "../../assets/images/cameron.png";
import jobelinPicture from "../../assets/images/jobelin.png";
import mathysPicture from "../../assets/images/mathys.png";
import benjaminPicture from "../../assets/images/benjamin.png";
import charlesPicture from "../../assets/images/charles.png";
import nasserPicture from "../../assets/images/nasser.png";
import douniaPicture from "../../assets/images/dounia.png";
import nassimPicture from "../../assets/images/nassim.png";
import daivePicture from "../../assets/images/daive.png";
import { useLanguage } from "../../features/LanguageContext";

function WhoAreWeSection() {
    // Array holding the images of the team members
    const images = [
        cameronPicture,
        jobelinPicture,
        mathysPicture,
        benjaminPicture,
        charlesPicture,
        nasserPicture,
        douniaPicture,
        nassimPicture,
        daivePicture,
    ];

    // Group images into sets of 3 for the carousel display
    const groupedImages = [];
    for (let i = 0; i < images.length; i += 3) {
        groupedImages.push(images.slice(i, i + 3)); // Create groups of 3 images
    }

    // State to track the current index for the image group being displayed
    const [currentIndex, setCurrentIndex] = useState(0);

    // Function to go to the next set of images in the carousel
    const nextSlide = () => {
        setCurrentIndex((prevIndex) => (prevIndex + 1) % groupedImages.length);
    };

    // Function to go to the previous set of images in the carousel
    const prevSlide = () => {
        setCurrentIndex(
            (prevIndex) => (prevIndex - 1 + groupedImages.length) % groupedImages.length
        );
    };

    // Language state to manage the language toggle (English or French)
    const { language } = useLanguage();

    // Text content for both languages (English and French)
    const text = {
        en: {
            header: "Who are we?", // Header in English
            description:
                "We’re a team of 9 passionate individuals dedicated to delivering fresh, high-quality fruits. With expertise and enthusiasm, we bring the best to our customers every day.",
        },
        fr: {
            header: "Qui sommes-nous ?", // Header in French
            description:
                "Nous sommes une équipe de 9 personnes passionnées, dédiée à la livraison de fruits frais et de haute qualité. Avec expertise et enthousiasme, nous apportons le meilleur à nos clients chaque jour.",
        },
    };

    return (
        <section id="aboutus" className="bg-primary py-7">
            {/* Section wrapper with layout for header and image carousel */}
            <div className="flex justify-between items-center w-full max-w-screen-xl mx-auto p-4">
                {/* Left content section (text) */}
                <div className="flex-1 text-left mr-16">
                    {/* Header of the section, dynamically rendered based on language */}
                    <h2 className="text-2xl font-semibold text-black font-semibold border-b-4 border-accent inline-block">
                        {text[language].header}
                    </h2>
                    {/* Description text for the section */}
                    <p className="text-base mt-4 text-black font-light mr-12">
                        {text[language].description}
                    </p>
                </div>

                {/* Image carousel section */}
                <div className="relative w-3/5 mx-auto overflow-hidden">
                    {/* Image carousel container */}
                    <div className="flex transition-transform duration-500 ease-in-out">
                        {/* Dynamically render images based on the current index */}
                        {groupedImages[currentIndex] && groupedImages[currentIndex].map((image, index) => (
                            <div key={index} className="w-1/3 p-1">
                                {/* Display each image inside a container */}
                                <img
                                    src={image}
                                    alt={`carousel-image-${index}`} // Unique alt text for accessibility
                                    className="w-full h-auto rounded-lg" // Image styling
                                />
                            </div>
                        ))}
                    </div>

                    {/* Previous button to go to the previous image set */}
                    <button
                        onClick={prevSlide}
                        className="absolute top-1/2 left-4 transform -translate-y-1/2 bg-black bg-opacity-50 text-white text-2xl p-2 rounded-md cursor-pointer z-10 hover:bg-opacity-80"
                    >
                        &#8592;
                    </button>

                    {/* Next button to go to the next image set */}
                    <button
                        onClick={nextSlide}
                        className="absolute top-1/2 right-4 transform -translate-y-1/2 bg-black bg-opacity-50 text-white text-2xl p-2 rounded-md cursor-pointer z-10 hover:bg-opacity-80"
                    >
                        &#8594;
                    </button>
                </div>
            </div>
        </section>
    );
}

export default WhoAreWeSection;
