import Article from "../../utils/models/Articles";
import { Translation } from "../../utils/translations-article-details";
import QuantitySelector from "./QuantitySelector";
import AddToCartButton from "./AddToCartButton";

interface ArticleInfoProps {
    article: Article;
    totalPrice: string;
    measurementType: "kg" | "unit";
    quantity: number;
    setMeasurementType: (value: "kg" | "unit") => void;
    onQuantityChange: (delta: number) => void;
    onQuantityInput: (value: number) => void;
    onAddToCart: (event: React.MouseEvent<HTMLButtonElement>) => void;
    orderId: number | null;
    translations: Translation;
}

const ArticleInfo: React.FC<ArticleInfoProps> = ({
                                                     article,
                                                     totalPrice,
                                                     measurementType,
                                                     quantity,
                                                     setMeasurementType,
                                                     onQuantityChange,
                                                     onQuantityInput,
                                                     onAddToCart,
                                                     orderId,
                                                     translations,
                                                 }) => {
    return (
        <div className="space-y-6 mt-12">
            <h1 className="text-4xl font-extrabold text-secondary leading-tight">{article.name}</h1>
            <p className="text-lg text-gray-700">{article.description}</p>
            <div className="text-md text-gray-600">
                <span className="font-semibold">{translations.origin}</span> {article.origin}
            </div>
            <div className="text-2xl font-semibold text-accent transition-all duration-200">
                {totalPrice} €{" "}
                <span className="text-gray-500 text-base ml-2">
          ({measurementType === "kg" ? `${article.priceKg} € / kg` : `${article.priceUnit} € / unit`})
        </span>
            </div>
            <div className="text-sm text-gray-600">
                <span className="font-semibold">{translations.stock}: </span>
                {article.stockKg} kg — {article.stockUnit} units available
            </div>
            <QuantitySelector
                quantity={quantity}
                measurementType={measurementType}
                setMeasurementType={setMeasurementType}
                onQuantityChange={onQuantityChange}
                onQuantityInput={onQuantityInput}
                article={article}
                translations={translations}
            />
            <AddToCartButton
                onAddToCart={onAddToCart}
                disabled={!orderId} // Disable the button if orderId is not defined
                translations={translations}
            />
        </div>
    );
};

export default ArticleInfo;