import {Translation} from "../../utils/translations-article-details";

interface AddToCartButtonProps {
    onAddToCart: (event: React.MouseEvent<HTMLButtonElement>) => void;
    disabled: boolean;
    translations: Translation;
}

const AddToCartButton: React.FC<AddToCartButtonProps> = ({ onAddToCart, disabled, translations }) => {
    return (
        <div className="flex gap-4 items-center mt-8">
            <button
                onClick={onAddToCart}
                disabled={disabled}
                className={`bg-accent text-white font-semibold px-8 py-4 rounded-lg shadow-lg transition transform hover:bg-[#D43F97] hover:scale-105 relative overflow-hidden ${
                    disabled ? "opacity-50 cursor-not-allowed" : ""
                }`}
            >
                {translations.addToCart}
                <span className="pulse-effect absolute inset-0 rounded-lg bg-white opacity-0"></span>
            </button>
        </div>
    );
};

export default AddToCartButton;