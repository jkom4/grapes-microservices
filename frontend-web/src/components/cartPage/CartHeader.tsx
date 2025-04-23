import { Translation } from "../../utils/translations-payment";

interface CartHeaderProps {
    translations: Translation;
}

const CartHeader: React.FC<CartHeaderProps> = ({ translations }) => {
    return (
        <h1 className="text-3xl font-bold text-secondary mb-6">{translations.checkout}</h1>
    );
};

export default CartHeader;