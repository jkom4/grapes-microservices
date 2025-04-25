import Article from "../../utils/models/Articles";
import { translationsAllArticles } from "../../utils/translations-all-articles";

interface ArticleModalProps {
    isOpen: boolean;
    article: Article | null;
    unitType: "kg" | "units";
    setUnitType: (value: "kg" | "units") => void;
    quantityKg: string;
    setQuantityKg: (value: string) => void;
    quantityUnits: string;
    setQuantityUnits: (value: string) => void;
    onClose: () => void;
    onAddToCart: () => void;
    translations: typeof translationsAllArticles["en"];
}

const ArticleModal: React.FC<ArticleModalProps> = ({
                                                       isOpen,
                                                       article,
                                                       unitType,
                                                       setUnitType,
                                                       quantityKg,
                                                       setQuantityKg,
                                                       quantityUnits,
                                                       setQuantityUnits,
                                                       onClose,
                                                       onAddToCart,
                                                       translations,
                                                   }) => {
    if (!isOpen || !article) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
                <h3 className="text-lg font-semibold mb-4">
                    {translations.modalTitle}: {article.name}
                </h3>
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">{translations.unitType}</label>
                        <select
                            value={unitType}
                            onChange={(e) => setUnitType(e.target.value as "kg" | "units")}
                            className="mt-1 block w-full border border-gray-300 rounded-lg p-2 focus:ring-2 focus:ring-accent"
                        >
                            <option value="units">{translations.quantityUnits}</option>
                            <option value="kg">{translations.quantityKg}</option>
                        </select>
                    </div>
                    {unitType === "kg" ? (
                        <div>
                            <label className="block text-sm font-medium text-gray-700">
                                {translations.quantityKg} (Max: {article.stockKg} kg)
                            </label>
                            <input
                                type="number"
                                value={quantityKg}
                                onChange={(e) => setQuantityKg(e.target.value)}
                                min="0"
                                step="0.1"
                                max={article.stockKg}
                                className="mt-1 block w-full border border-gray-300 rounded-lg p-2 focus:ring-2 focus:ring-accent"
                                placeholder="e.g., 1.5"
                            />
                        </div>
                    ) : (
                        <div>
                            <label className="block text-sm font-medium text-gray-700">
                                {translations.quantityUnits} (Max: {article.stockUnit} units)
                            </label>
                            <input
                                type="number"
                                value={quantityUnits}
                                onChange={(e) => setQuantityUnits(e.target.value)}
                                min="1"
                                max={article.stockUnit}
                                className="mt-1 block w-full border border-gray-300 rounded-lg p-2 focus:ring-2 focus:ring-accent"
                                placeholder="e.g., 1"
                            />
                        </div>
                    )}
                </div>
                <div className="mt-6 flex justify-end space-x-2">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400"
                    >
                        {translations.cancelButton}
                    </button>
                    <button
                        onClick={onAddToCart}
                        className="px-4 py-2 bg-secondary text-white rounded-lg hover:bg-accent"
                    >
                        {translations.addButton}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ArticleModal;