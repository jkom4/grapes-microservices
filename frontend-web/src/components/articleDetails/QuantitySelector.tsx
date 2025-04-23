import Article from "../../utils/models/Articles";
import {Translation} from "../../utils/translations-article-details";

interface QuantitySelectorProps {
    quantity: number;
    measurementType: "kg" | "unit";
    setMeasurementType: (value: "kg" | "unit") => void;
    onQuantityChange: (delta: number) => void;
    onQuantityInput: (value: number) => void;
    article: Article;
    translations: Translation;
}

const QuantitySelector: React.FC<QuantitySelectorProps> = ({
                                                               quantity,
                                                               measurementType,
                                                               setMeasurementType,
                                                               onQuantityChange,
                                                               onQuantityInput,
                                                               article,
                                                               translations,
                                                           }) => {
    return (
        <>
            {/* Measurement type selector */}
            <div className="mt-4">
                <label className="text-sm font-semibold text-gray-700 mb-2 block">{translations.stock}</label>
                <div className="flex space-x-2">
                    <button
                        onClick={() => {
                            setMeasurementType("kg");
                            onQuantityInput(1);
                        }}
                        className={`px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 ${
                            measurementType === "kg"
                                ? "bg-accent text-white"
                                : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                        }`}
                    >
                        {translations.kg}
                    </button>
                    <button
                        onClick={() => {
                            setMeasurementType("unit");
                            onQuantityInput(1);
                        }}
                        className={`px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 ${
                            measurementType === "unit"
                                ? "bg-accent text-white"
                                : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                        }`}
                    >
                        {translations.unit}
                    </button>
                </div>
            </div>

            {/* Quantity selector */}
            <div className="flex items-center mt-4">
                <div className="flex items-center border border-gray-300 rounded-lg overflow-hidden">
                    <button
                        onClick={() => onQuantityChange(-1)}
                        disabled={quantity <= 1}
                        className="px-4 py-2 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        -
                    </button>
                    <input
                        type="number"
                        value={quantity}
                        onChange={(e) => onQuantityInput(parseInt(e.target.value))}
                        className="w-16 h-10 text-center border-none focus:ring-0 appearance-none"
                    />
                    <button
                        onClick={() => onQuantityChange(1)}
                        disabled={quantity >= (measurementType === "kg" ? article.stockKg : article.stockUnit)}
                        className="px-4 py-2 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        +
                    </button>
                </div>
                <span className="ml-2 text-sm text-gray-600">
          {measurementType === "kg" ? translations.kg : translations.unit}
        </span>
            </div>
        </>
    );
};

export default QuantitySelector;