import NewsletterImage from "../../assets/images/newsletter.png";
import { useLanguage } from "../../features/LanguageContext";

function Newsletter() {
    // Language state to manage the selected language
    const { language } = useLanguage();

    // Text content for the newsletter in both English and French
    const text = {
        en: {
            header: "Subscribe to Our Newsletter",
            placeholder: "Enter your email",
            button: "Submit",
        },
        fr: {
            header: "Abonnez-vous à notre Newsletter",
            placeholder: "Entrez votre email",
            button: "Envoyer",
        }
    };

    return (
        <section className="bg-white flex justify-center items-center min-h-[300px] py-10 mt-14">
            <div className="relative w-4/5 text-center">
                {/* Image section with a rounded container */}
                <div className="w-full h-[300px] overflow-hidden rounded-[100px] mb-10">
                    <img
                        src={NewsletterImage}
                        alt="Fruit"
                        className="w-full h-full object-cover rounded-lg"
                    />
                </div>
                <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-full">
                    {/* Header text in the selected language */}
                    <h2 className="text-white font-semibold text-2xl">{text[language].header}</h2>
                    {/* Email input field with dynamic placeholder text */}
                    <input
                        type="email"
                        placeholder={text[language].placeholder}
                        className="mt-4 p-3 text-base rounded-[20px_0_0_20px] w-[300px] focus:outline-none"
                    />
                    {/* Submit button with dynamic text */}
                    <button className="mt-4 py-3 px-5 font-semibold text-base text-white bg-secondary rounded-[0_20px_20px_0] hover:bg-accent">
                        {text[language].button}
                    </button>
                </div>
            </div>
        </section>
    );
}

export default Newsletter;
