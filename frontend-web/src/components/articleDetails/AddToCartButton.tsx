import {Translation} from "../../utils/translations-article-details";

interface AddToCartButtonProps {
    onAddToCart: (event: React.MouseEvent<HTMLButtonElement>) => void;
    translations: Translation;
}

const AddToCartButton: React.FC<AddToCartButtonProps> = ({ onAddToCart, translations }) => {
    return (
        <div className="flex gap-4 items-center mt-8">
            <button
                onClick={onAddToCart}
                className={`bg-accent text-white font-semibold px-8 py-4 rounded-lg shadow-lg transition transform hover:bg-[#D43F97] hover:scale-105 relative overflow-hidden
                }`}
            >
                {translations.addToCart}
                <span className="pulse-effect absolute inset-0 rounded-lg bg-white opacity-0"></span>
            </button>
        </div>
    );
};

export default AddToCartButton;