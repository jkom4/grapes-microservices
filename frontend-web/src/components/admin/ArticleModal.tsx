import {ArticleModalProps} from "../../utils/models/interface/ArticleModalProps";

export const ArticleModal: React.FC<ArticleModalProps> =
    ({
         isOpen,
         editingArticle,
         formData,
         errorMessage,
         onClose,
         onSubmit,
         onInputChange,
         translations,
     }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-60 flex justify-center items-center p-4">
            <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg flex flex-col max-h-[90vh]">
                {/* Modal Header */}
                <div className="px-8 py-6 border-b border-gray-200">
                    <h2 className="text-2xl font-bold text-gray-900">
                        {editingArticle ? translations.modal_edit_article_title : translations.modal_add_article_title}
                    </h2>
                </div>
                {/* Scrollable Form Content */}
                <div className="flex-1 overflow-y-auto px-8 py-6">
                    {errorMessage && (
                        <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-lg">
                            {errorMessage}
                        </div>
                    )}
                    <form onSubmit={onSubmit} className="space-y-5">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">{translations.modal_label_name}</label>
                            <input
                                type="text"
                                name="name"
                                value={formData.name || ''}
                                onChange={onInputChange}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">{translations.modal_label_description}</label>
                            <textarea
                                name="description"
                                value={formData.description || ''}
                                onChange={onInputChange}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 resize-y"
                                rows={4}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">{translations.modal_label_category_id}</label>
                            <input
                                type="number"
                                name="categoryId"
                                value={formData.categoryId || 0}
                                onChange={onInputChange}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">{translations.modal_label_family_id}</label>
                            <input
                                type="number"
                                name="familyId"
                                value={formData.familyId || 0}
                                onChange={onInputChange}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">{translations.modal_label_price_kg}</label>
                            <input
                                type="number"
                                name="priceKg"
                                value={formData.priceKg || 0}
                                onChange={onInputChange}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                step="0.01"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">{translations.modal_label_price_unit}</label>
                            <input
                                type="number"
                                name="priceUnit"
                                value={formData.priceUnit || 0}
                                onChange={onInputChange}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                step="0.01"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">{translations.modal_label_stock_kg}</label>
                            <input
                                type="number"
                                name="stockKg"
                                value={formData.stockKg || 0}
                                onChange={onInputChange}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">{translations.modal_label_stock_unit}</label>
                            <input
                                type="number"
                                name="stockUnit"
                                value={formData.stockUnit || 0}
                                onChange={onInputChange}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">{translations.modal_label_origin}</label>
                            <input
                                type="text"
                                name="origin"
                                value={formData.origin || ''}
                                onChange={onInputChange}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">{translations.modal_label_picture_path}</label>
                            <input
                                type="text"
                                name="picturePath"
                                value={formData.picturePath || ''}
                                onChange={onInputChange}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                            />
                        </div>
                    </form>
                </div>
                {/* Modal Footer */}
                <div className="px-8 py-6 border-t border-gray-200 flex justify-end space-x-3 bg-gray-50 rounded-b-xl">
                    <button
                        type="button"
                        onClick={onClose}
                        className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-all duration-200"
                    >
                        {translations.modal_button_cancel}
                    </button>
                    <button
                        type="submit"
                        onClick={onSubmit}
                        className="px-6 py-3 bg-gradient-to-r from-secondary to-accent text-white rounded-lg shadow-md hover:from-secondary hover:to-primary transition-all duration-200"
                    >
                        {editingArticle ? translations.modal_button_update_article : translations.modal_button_add_article}
                    </button>
                </div>
            </div>
        </div>
    );
};