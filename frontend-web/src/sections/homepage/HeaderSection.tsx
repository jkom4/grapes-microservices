import fruitImage from "../../assets/images/grapes.png";
import { useLanguage } from "../../features/LanguageContext";

// HeaderSection Component: Displays a hero section with text and an image
const HeaderSection: React.FC = () => {
    const { language } = useLanguage();  // State to manage language toggle

    // Text content in both languages (English and French)
    const text = {
        en: {
            heading: "Enjoy your fruits\nbefore your activity",
            description: "Elevate your day and energize your business\nwith the fresh, vibrant taste of our premium fruits.",
            button: "Order now"
        },
        fr: {
            heading: "Savourez vos fruits\navant votre activité",
            description: "Boostez votre journée et dynamisez votre activité\navec le goût frais et vibrant de nos fruits premium.",
            button: "Commander maintenant"
        }
    };

    return (
        <section className="flex justify-center items-center bg-primary py-0">
            <div className="flex w-full max-w-screen-xl items-center justify-between">
                <div className="hero-text flex-1 text-left">
                    {/* Heading text with dynamic language and styling */}
                    <h1 className="text-5xl font-semibold text-secondary">
                        {text[language].heading.split("\n")[0]} <span className="text-pink-500">fruits</span>
                        <br /> {text[language].heading.split("\n")[1]}
                    </h1>
                    {/* Description text in selected language */}
                    <p className="mt-4 text-black font-normal">
                        {text[language].description}
                    </p>
                    {/* Button to order fruits, text changes based on language */}
                    <button className="mt-5 px-6 py-3 bg-secondary text-white rounded-full font-semibold text-sm hover:bg-accent transition-colors duration-300">
                        {text[language].button}
                    </button>
                </div>
                <div className="hero-image flex-1 flex justify-end">
                    {/* Image of the fruit (hero image), aligned to the right */}
                    <img src={fruitImage} alt="Healthy fruit bowl" className="w-3/4" />
                </div>
            </div>
        </section>
    );
};

export default HeaderSection;
