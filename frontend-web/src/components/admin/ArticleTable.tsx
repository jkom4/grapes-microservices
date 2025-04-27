import {ArticleTableProps} from "../../utils/models/interface/ArticleTabsProps";

export const ArticleTable: React.FC<ArticleTableProps> = ({ articles, onEdit, onDelete, translations }) => {
    return (
        <div className="bg-white shadow-lg rounded-lg overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
            <tr>
                <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">{translations.column_id}</th>
                <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">{translations.column_name}</th>
                <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">{translations.column_price_kg}</th>
                <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">{translations.column_price_unit}</th>
                <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">{translations.column_stock_kg}</th>
                <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">{translations.column_stock_unit}</th>
                <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">{translations.column_origin}</th>
                <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">{translations.column_actions}</th>
            </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
            {articles.map((article) => (
                <tr key={article.id} className="hover:bg-gray-50 transition-colors">
                    <td className="py-4 px-6 text-sm text-gray-700">{article.id}</td>
                    <td className="py-4 px-6 text-sm text-gray-700">{article.name}</td>
                    <td className="py-4 px-6 text-sm text-gray-700">€{article.priceKg.toFixed(2)}</td>
                    <td className="py-4 px-6 text-sm text-gray-700">€{article.priceUnit.toFixed(2)}</td>
                    <td className="py-4 px-6 text-sm text-gray-700">{article.stockKg}</td>
                    <td className="py-4 px-6 text-sm text-gray-700">{article.stockUnit}</td>
                    <td className="py-4 px-6 text-sm text-gray-700">{article.origin}</td>
                    <td className="py-4 px-6 text-sm">
                        <button
                            onClick={() => onEdit(article.id)}
                            className="text-accent hover:text-secondary transition-colors"
                        >
                            ✏️
                        </button>
                        <button
                            onClick={() => onDelete(article.id)}
                            className="text-red-600 hover:text-red-900"
                            aria-label={`Delete ${article.name}`}
                        >
                            🗑️
                        </button>
                    </td>
                </tr>
            ))}
            </tbody>
        </table>
    </div>
    );
};