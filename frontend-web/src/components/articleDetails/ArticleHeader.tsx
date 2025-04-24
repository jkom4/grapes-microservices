import { Translation } from "../../utils/translations-article-details";

interface ArticleHeaderProps {
    isFavorite: boolean;
    setIsFavorite: (value: boolean) => void;
    onBack: () => void;
    translations: Translation;
}

const ArticleHeader: React.FC<ArticleHeaderProps> = ({ isFavorite, setIsFavorite, onBack, translations }) => {
    return (
        <>
            {/* Back button */}
            <div className="absolute top-0 left-4 flex items-center text-secondary hover:text-accent transition">
                <button
                    onClick={onBack}
                    aria-label={translations.back}
                    className="flex items-center"
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="w-6 h-6 mr-2"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    >
                        <path d="M19 12H5"></path>
                        <path d="M12 19l-7-7 7-7"></path>
                    </svg>
                    <span>{translations.back}</span>
                </button>
            </div>

            {/* Favorite button */}
            <button
                onClick={() => setIsFavorite(!isFavorite)}
                className={`absolute top-16 right-4 w-12 h-12 rounded-full flex items-center justify-center transition ${
                    isFavorite ? "bg-red-500" : "bg-white border border-gray-300 hover:bg-gray-100"
                }`}
                aria-label="Favorite"
            >
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className={`w-6 h-6 ${isFavorite ? "text-white" : "text-gray-600"}`}
                    viewBox="0 0 24 24"
                    fill="currentColor"
                >
                    <path
                        fillRule="evenodd"
                        d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                        clipRule="evenodd"
                    />
                </svg>
            </button>
        </>
    );
};

export default ArticleHeader;