import React, { useEffect, useState } from 'react';
import { fetchFruits, fetchArticleById, addArticle, updateArticle } from "../../services/fruitServices";
import Article from "../../utils/models/Articles";

const AdminSection: React.FC = () => {
    const [articles, setArticles] = useState<Article[]>([]);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [editingArticle, setEditingArticle] = useState<Article | null>(null);
    const [formData, setFormData] = useState<Partial<Article>>({
        name: '',
        description: '',
        priceKg: 0,
        priceUnit: 0,
        stockKg: 0,
        stockUnit: 0,
        origin: '',
        picturePath: '',
        categoryId: 0,
        familyId: 0,
    });

    const pageSize = 10;

    // Fetch articles on component mount and page change
    useEffect(() => {
        const loadArticles = async () => {
            try {
                const { content, totalPages } = await fetchFruits(currentPage, pageSize);
                setArticles(content);
                setTotalPages(totalPages);
            } catch (error) {
                console.error('Error fetching articles:', error);
            }
        };
        loadArticles();
    }, [currentPage]);

    // Handle form input changes
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: name.includes('Id') || name.includes('price') || name.includes('stock') ? Number(value) : value,
        }));
    };

    // Handle form submission for adding/updating article
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const articleData: Article = {
                ...formData,
                id: editingArticle ? editingArticle.id : undefined,
            } as Article;

            if (editingArticle) {
                await updateArticle(editingArticle.id, articleData);
            } else {
                await addArticle(articleData);
            }

            // Refresh articles
            const { content, totalPages } = await fetchFruits(currentPage, pageSize);
            setArticles(content);
            setTotalPages(totalPages);
            closeModal();
        } catch (error) {
            console.error('Error saving article:', error);
        }
    };

    // Open modal for adding new article
    const openAddModal = () => {
        setEditingArticle(null);
        setFormData({
            name: '',
            description: '',
            priceKg: 0,
            priceUnit: 0,
            stockKg: 0,
            stockUnit: 0,
            origin: '',
            picturePath: '',
            categoryId: 0,
            familyId: 0,
        });
        setIsModalOpen(true);
    };

    // Open modal for editing existing article
    const openEditModal = async (id: number) => {
        try {
            const article = await fetchArticleById(id);
            setEditingArticle(article);
            setFormData({
                ...article,
            });
            setIsModalOpen(true);
        } catch (error) {
            console.error('Error fetching article:', error);
        }
    };

    // Close modal
    const closeModal = () => {
        setIsModalOpen(false);
        setEditingArticle(null);
    };

    return (
        <div className="min-h-screen bg-gray-100 p-8">
            <div className="max-w-7xl mx-auto">
                <div className="flex justify-between items-center mb-8">
                    <h1 className="text-3xl font-extrabold text-gray-900">Article Management</h1>
                    <button
                        onClick={openAddModal}
                        className="flex items-center bg-gradient-to-r from-accent to-secondary text-white px-6 py-3 rounded-lg shadow-md hover:from-pink-200 hover:to-red-300 transition-all duration-200"
                    >
                        <span className="mr-2">+</span>
                        Add New Article
                    </button>
                </div>

                {/* Articles Table */}
                <div className="bg-white shadow-lg rounded-lg overflow-hidden">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">ID</th>
                            <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">Name</th>
                            <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">Price/Kg</th>
                            <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">Price/Unit</th>
                            <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">Stock/Kg</th>
                            <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">Stock/Unit</th>
                            <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">Origin</th>
                            <th className="py-4 px-6 text-left text-sm font-semibold text-gray-900">Actions</th>
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
                                        onClick={() => openEditModal(article.id)}
                                        className="text-accent hover:text-secondary transition-colors"
                                    >
                                        ✏️
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                {/* Pagination */}
                <div className="mt-6 flex justify-center items-center space-x-4">
                    <button
                        onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
                        disabled={currentPage === 0}
                        className="px-4 py-2 bg-accent text-white rounded-lg shadow-md hover:bg-secondary disabled:bg-gray-300 disabled:cursor-not-allowed transition-all duration-200"
                    >
                        Previous
                    </button>
                    <span className="text-sm text-gray-700">
                        Page {currentPage + 1} of {totalPages}
                    </span>
                    <button
                        onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages - 1))}
                        disabled={currentPage >= totalPages - 1}
                        className="px-4 py-2 bg-accent text-white rounded-lg shadow-md hover:bg-secondary disabled:bg-gray-300 disabled:cursor-not-allowed transition-all duration-200"
                    >
                        Next
                    </button>
                </div>

                {/* Modal for Add/Edit Article */}
                {isModalOpen && (
                    <div className="fixed inset-0 bg-black bg-opacity-60 flex justify-center items-center p-4">
                        <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg flex flex-col max-h-[90vh]">
                            {/* Modal Header */}
                            <div className="px-8 py-6 border-b border-gray-200">
                                <h2 className="text-2xl font-bold text-gray-900">
                                    {editingArticle ? 'Edit Article' : 'Add Article'}
                                </h2>
                            </div>
                            {/* Scrollable Form Content */}
                            <div className="flex-1 overflow-y-auto px-8 py-6">
                                <form onSubmit={handleSubmit} className="space-y-5">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Name</label>
                                        <input
                                            type="text"
                                            name="name"
                                            value={formData.name}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Description</label>
                                        <textarea
                                            name="description"
                                            value={formData.description}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 resize-y"
                                            rows={4}
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Category ID</label>
                                        <input
                                            type="number"
                                            name="categoryId"
                                            value={formData.categoryId}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Family ID</label>
                                        <input
                                            type="number"
                                            name="familyId"
                                            value={formData.familyId}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Price/Kg</label>
                                        <input
                                            type="number"
                                            name="priceKg"
                                            value={formData.priceKg}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                            step="0.01"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Price/Unit</label>
                                        <input
                                            type="number"
                                            name="priceUnit"
                                            value={formData.priceUnit}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                            step="0.01"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Stock/Kg</label>
                                        <input
                                            type="number"
                                            name="stockKg"
                                            value={formData.stockKg}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Stock/Unit</label>
                                        <input
                                            type="number"
                                            name="stockUnit"
                                            value={formData.stockUnit}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Origin</label>
                                        <input
                                            type="text"
                                            name="origin"
                                            value={formData.origin}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Picture Path</label>
                                        <input
                                            type="text"
                                            name="picturePath"
                                            value={formData.picturePath}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                                        />
                                    </div>
                                </form>
                            </div>
                            {/* Modal Footer */}
                            <div className="px-8 py-6 border-t border-gray-200 flex justify-end space-x-3 bg-gray-50 rounded-b-xl">
                                <button
                                    type="button"
                                    onClick={closeModal}
                                    className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-all duration-200"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    onClick={handleSubmit}
                                    className="px-6 py-3 bg-gradient-to-r from-secondary to-accent text-white rounded-lg shadow-md hover:from-secondary hover:to-primary transition-all duration-200"
                                >
                                    {editingArticle ? 'Update Article' : 'Add Article'}
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AdminSection;