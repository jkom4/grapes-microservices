import { useLanguage } from "../../features/LanguageContext";
import Article from "../../utils/models/Articles";
import { useEffect, useState } from "react";
import { addArticle, fetchArticleById, fetchFruits, updateArticle, deleteArticle } from "../../services/fruitServices";
import { AdminHeader } from "../../components/admin/AdminHeader";
import { ArticleTable } from "../../components/admin/ArticleTable";
import { PaginationControls } from "../../components/admin/PaginationControls";
import { ArticleModal } from "../../components/admin/ArticleModal";
import { translationsAdmin } from "../../utils/translations-admin";
import "react-toastify/dist/ReactToastify.css";
import { toast } from "react-toastify";


const AdminSection: React.FC = () => {
    const { language } = useLanguage();
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
    const [errorMessage, setErrorMessage] = useState<string>('');

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
        setErrorMessage('');
    };

    // Handle form submission for adding/updating article
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        toast.success(text[language].article_save_success);
        setErrorMessage('');
        try {
            // Client-side validation
            if (!formData.name?.trim()) {
            throw new Error(translationsAdmin[language].error_name_required);
            }
            // Ensure categoryId and familyId are numbers and positive
            const categoryId = formData.categoryId ?? 1;
            const familyId = formData.familyId ?? 1;
            if (categoryId <= 0) {
            throw new Error(translationsAdmin[language].error_category_id_positive);
            }
            if (familyId <= 0) {
            throw new Error(translationsAdmin[language].error_family_id_positive);
            }
            if (formData.priceKg && formData.priceKg < 0 || formData.priceUnit && formData.priceUnit < 0) {
            throw new Error(translationsAdmin[language].error_prices_negative);
            }
            if (formData.stockKg && formData.stockKg < 0 || formData.stockUnit && formData.stockUnit < 0) {
            throw new Error(translationsAdmin[language].error_stock_negative);
            }

            // Construct articleData with guaranteed numbers
            const articleData: Article = {
                ...formData,
                categoryId,
                familyId,
                name: formData.name || '',
                description: formData.description || '',
                priceKg: formData.priceKg ?? 0,
                priceUnit: formData.priceUnit ?? 0,
                stockKg: formData.stockKg ?? 0,
                stockUnit: formData.stockUnit ?? 0,
                origin: formData.origin || '',
                picturePath: formData.picturePath || '',
                id: editingArticle ? editingArticle.id : undefined,
            } as Article;

            if (editingArticle) {
                await updateArticle(editingArticle.id, articleData);
            } else {
                // Remove id for new articles
                const { id, ...articleDataWithoutId } = articleData;
                await addArticle(articleDataWithoutId as Article);
            }

            // Refresh articles
            const { content, totalPages } = await fetchFruits(currentPage, pageSize);
            setArticles(content);
            setTotalPages(totalPages);
            closeModal();
        } catch (error) {
            console.error('Error saving article:', error);
        setErrorMessage(error instanceof Error ? error.message : translationsAdmin[language].error_saving_article);
        }
    };

    // Handle article deletion
    const handleDelete = async (id: number) => {
        if (!window.confirm("Are you sure you want to delete this article?")) {
            return;
        }
        try {
            await deleteArticle(id);
            // Refresh articles
            const { content, totalPages } = await fetchFruits(currentPage, pageSize);
            setArticles(content);
            setTotalPages(totalPages);
            toast.success(text[language].article_save_success);
            // If the current page is empty after deletion, go to the previous page
            if (content.length === 0 && currentPage > 0) {
                setCurrentPage(currentPage - 1);
            }
        } catch (error) {
            console.error('Error deleting article:', error);
        setErrorMessage(error instanceof Error ? error.message : translationsAdmin[language].error_deleting_article);
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
            categoryId: 1,
            familyId: 1,
        });
        setErrorMessage('');
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
            setErrorMessage('');
            setIsModalOpen(true);
        } catch (error) {
            console.error('Error fetching article:', error);
        }
    };

    // Close modal
    const closeModal = () => {
        setIsModalOpen(false);
        setEditingArticle(null);
        setErrorMessage('');
    };

    return (
        <div className="min-h-screen bg-gray-100 p-8">
            <div className="max-w-7xl mx-auto">
                <AdminHeader
                title={translationsAdmin[language].header}
                addButtonText={translationsAdmin[language].add_new_article}
                plusSign={translationsAdmin[language].plus_sign}
                    onAddClick={openAddModal}
                />
                <ArticleTable
                    articles={articles}
                    onEdit={openEditModal}
                    onDelete={handleDelete}
                    translations={{
                    column_id: translationsAdmin[language].column_id,
                    column_name: translationsAdmin[language].column_name,
                    column_price_kg: translationsAdmin[language].column_price_kg,
                    column_price_unit: translationsAdmin[language].column_price_unit,
                    column_stock_kg: translationsAdmin[language].column_stock_kg,
                    column_stock_unit: translationsAdmin[language].column_stock_unit,
                    column_origin: translationsAdmin[language].column_origin,
                    column_actions: translationsAdmin[language].column_actions,
                    }}
                />
                <PaginationControls
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPrevious={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
                    onNext={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages - 1))}
                    translations={{
                    pagination_previous: translationsAdmin[language].pagination_previous,
                    pagination_next: translationsAdmin[language].pagination_next,
                    pagination_page: translationsAdmin[language].pagination_page,
                    }}
                />
                <ArticleModal
                    isOpen={isModalOpen}
                    editingArticle={editingArticle}
                    formData={formData}
                    errorMessage={errorMessage}
                    onClose={closeModal}
                    onSubmit={handleSubmit}
                    onInputChange={handleInputChange}
                    translations={{
                    modal_add_article_title: translationsAdmin[language].modal_add_article_title,
                    modal_edit_article_title: translationsAdmin[language].modal_edit_article_title,
                    modal_label_name: translationsAdmin[language].modal_label_name,
                    modal_label_description: translationsAdmin[language].modal_label_description,
                    modal_label_category_id: translationsAdmin[language].modal_label_category_id,
                    modal_label_family_id: translationsAdmin[language].modal_label_family_id,
                    modal_label_price_kg: translationsAdmin[language].modal_label_price_kg,
                    modal_label_price_unit: translationsAdmin[language].modal_label_price_unit,
                    modal_label_stock_kg: translationsAdmin[language].modal_label_stock_kg,
                    modal_label_stock_unit: translationsAdmin[language].modal_label_stock_unit,
                    modal_label_origin: translationsAdmin[language].modal_label_origin,
                    modal_label_picture_path: translationsAdmin[language].modal_label_picture_path,
                    modal_button_cancel: translationsAdmin[language].modal_button_cancel,
                    modal_button_add_article: translationsAdmin[language].modal_button_add_article,
                    modal_button_update_article: translationsAdmin[language].modal_button_update_article,
                    }}
                />
            </div>
        </div>
    );
};

export default AdminSection;