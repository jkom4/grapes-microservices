import aboutUs from "../../assets/images/aboutUs.png";
import { useLanguage } from "../../features/LanguageContext";

function AboutUsSection() {
    const { language } = useLanguage();  // Access the current language from context

    // Text content in both languages (English and French)
    const text = {
        en: {
            header: "About us",
            subheader: "We offer premium fruits, always fresh and ready to deliver.",
            description: "We are a leading wholesaler specializing in fresh fruits, delivering high-quality produce globally. Our flagship product, crafted with a secret recipe.",
            button: "Get your fruits"
        },
        fr: {
            header: "À propos de nous",
            subheader: "Nous offrons des fruits premium, toujours frais et prêts à être livrés.",
            description: "Nous sommes un grossiste de premier plan, spécialisé dans les fruits frais, livrant des produits de haute qualité à l'échelle mondiale. Notre produit phare, élaboré avec une recette secrète.",
            button: "Obtenez vos fruits"
        }
    };

    return (
        <section className="relative bg-primary py-20">
            <div className="max-w-6xl mx-auto flex items-center justify-between">
                <div className="relative flex-1 flex justify-center">
                    {/* Image showcasing healthy fruit bowl */}
                    <img
                        src={aboutUs}
                        alt="Healthy fruit bowl"
                        className="rounded-2xl shadow-lg w-[400px] h-auto"
                    />
                </div>
                <div className="flex-1 text-left pl-12">
                    {/* Header and subheader text based on the current language */}
                    <h2 className="text-2xl font-semibold text-black">
                        {text[language].header}
                    </h2>
                    <h3 className="text-xl font-bold mt-4 text-black">
                        {text[language].subheader}
                    </h3>
                    {/* Description text in the selected language */}
                    <p className="mt-4 text-black text-base">
                        {text[language].description}
                    </p>
                    {/* Button text in the selected language */}
                    <button className="mt-6 px-6 py-3 bg-secondary text-white font-semibold rounded-full text-sm shadow-md">
                        {text[language].button}
                    </button>
                </div>
            </div>
        </section>
    );
}

export default AboutUsSection;
