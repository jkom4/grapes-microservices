// src/components/AdminSection.tsx
import { useLanguage } from "../../features/LanguageContext";
import Article from "../../utils/models/Articles";
import { useEffect, useState } from "react";
import { addArticle, fetchArticleById, fetchFruits, updateArticle, deleteArticle } from "../../services/fruitServices";
import { AdminHeader } from "../../components/admin/AdminHeader";
import { ArticleTable } from "../../components/admin/ArticleTable";
import { PaginationControls } from "../../components/admin/PaginationControls";
import { ArticleModal } from "../../components/admin/ArticleModal";

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

    const text = {
        en: {
            header: "Article Management",
            add_new_article: "Add New Article",
            plus_sign: "+",
            column_id: "ID",
            column_name: "Name",
            column_price_kg: "Price/Kg",
            column_price_unit: "Price/Unit",
            column_stock_kg: "Stock/Kg",
            column_stock_unit: "Stock/Unit",
            column_origin: "Origin",
            column_actions: "Actions",
            pagination_previous: "Previous",
            pagination_next: "Next",
            pagination_page: "Page {0} of {1}",
            modal_add_article_title: "Add Article",
            modal_edit_article_title: "Edit Article",
            modal_label_name: "Name",
            modal_label_description: "Description",
            modal_label_category_id: "Category ID",
            modal_label_family_id: "Family ID",
            modal_label_price_kg: "Price/Kg",
            modal_label_price_unit: "Price/Unit",
            modal_label_stock_kg: "Stock/Kg",
            modal_label_stock_unit: "Stock/Unit",
            modal_label_origin: "Origin",
            modal_label_picture_path: "Picture Path",
            modal_button_cancel: "Cancel",
            modal_button_add_article: "Add Article",
            modal_button_update_article: "Update Article",
            error_name_required: "Name is required",
            error_category_id_positive: "Category ID must be a positive number",
            error_family_id_positive: "Family ID must be a positive number",
            error_prices_negative: "Prices cannot be negative",
            error_stock_negative: "Stock values cannot be negative",
            error_saving_article: "An error occurred while saving the article",
            error_deleting_article: "An error occurred while deleting the article",
        },
        fr: {
            header: "Gestion des Articles",
            add_new_article: "Ajouter un Nouvel Article",
            plus_sign: "+",
            column_id: "ID",
            column_name: "Nom",
            column_price_kg: "Prix/Kg",
            column_price_unit: "Prix/Unité",
            column_stock_kg: "Stock/Kg",
            column_stock_unit: "Stock/Unité",
            column_origin: "Origine",
            column_actions: "Actions",
            pagination_previous: "Précédent",
            pagination_next: "Suivant",
            pagination_page: "Page {0} de {1}",
            modal_add_article_title: "Ajouter un Article",
            modal_edit_article_title: "Modifier l'Article",
            modal_label_name: "Nom",
            modal_label_description: "Description",
            modal_label_category_id: "ID de Catégorie",
            modal_label_family_id: "ID de Famille",
            modal_label_price_kg: "Prix/Kg",
            modal_label_price_unit: "Prix/Unité",
            modal_label_stock_kg: "Stock/Kg",
            modal_label_stock_unit: "Stock/Unité",
            modal_label_origin: "Origine",
            modal_label_picture_path: "Chemin de l'Image",
            modal_button_cancel: "Annuler",
            modal_button_add_article: "Ajouter un Article",
            modal_button_update_article: "Mettre à Jour l'Article",
            error_name_required: "Le nom est requis",
            error_category_id_positive: "L'ID de catégorie doit être un nombre positif",
            error_family_id_positive: "L'ID de famille doit être un nombre positif",
            error_prices_negative: "Les prix ne peuvent pas être négatifs",
            error_stock_negative: "Les valeurs de stock ne peuvent pas être négatives",
            error_saving_article: "Une erreur s'est produite lors de l'enregistrement de l'article",
            error_deleting_article: "Une erreur s'est produite lors de la suppression de l'article",
        },
    };

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
        setErrorMessage('');
        try {
            // Client-side validation
            if (!formData.name?.trim()) {
                throw new Error(text[language].error_name_required);
            }
            // Ensure categoryId and familyId are numbers and positive
            const categoryId = formData.categoryId ?? 1;
            const familyId = formData.familyId ?? 1;
            if (categoryId <= 0) {
                throw new Error(text[language].error_category_id_positive);
            }
            if (familyId <= 0) {
                throw new Error(text[language].error_family_id_positive);
            }
            if ((formData.priceKg && formData.priceKg < 0 )|| (formData.priceUnit && formData.priceUnit < 0)) {
                throw new Error(text[language].error_prices_negative);
            }
            if ((formData.stockKg && formData.stockKg < 0 )|| (formData.stockUnit && formData.stockUnit < 0)) {
                throw new Error(text[language].error_stock_negative);
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
            setErrorMessage(error instanceof Error ? error.message : text[language].error_saving_article);
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
            // If the current page is empty after deletion, go to the previous page
            if (content.length === 0 && currentPage > 0) {
                setCurrentPage(currentPage - 1);
            }
        } catch (error) {
            console.error('Error deleting article:', error);
            setErrorMessage(error instanceof Error ? error.message : text[language].error_deleting_article);
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
                    title={text[language].header}
                    addButtonText={text[language].add_new_article}
                    plusSign={text[language].plus_sign}
                    onAddClick={openAddModal}
                />
                <ArticleTable
                    articles={articles}
                    onEdit={openEditModal}
                    onDelete={handleDelete}
                    translations={{
                        column_id: text[language].column_id,
                        column_name: text[language].column_name,
                        column_price_kg: text[language].column_price_kg,
                        column_price_unit: text[language].column_price_unit,
                        column_stock_kg: text[language].column_stock_kg,
                        column_stock_unit: text[language].column_stock_unit,
                        column_origin: text[language].column_origin,
                        column_actions: text[language].column_actions,
                    }}
                />
                <PaginationControls
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPrevious={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
                    onNext={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages - 1))}
                    translations={{
                        pagination_previous: text[language].pagination_previous,
                        pagination_next: text[language].pagination_next,
                        pagination_page: text[language].pagination_page,
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
                        modal_add_article_title: text[language].modal_add_article_title,
                        modal_edit_article_title: text[language].modal_edit_article_title,
                        modal_label_name: text[language].modal_label_name,
                        modal_label_description: text[language].modal_label_description,
                        modal_label_category_id: text[language].modal_label_category_id,
                        modal_label_family_id: text[language].modal_label_family_id,
                        modal_label_price_kg: text[language].modal_label_price_kg,
                        modal_label_price_unit: text[language].modal_label_price_unit,
                        modal_label_stock_kg: text[language].modal_label_stock_kg,
                        modal_label_stock_unit: text[language].modal_label_stock_unit,
                        modal_label_origin: text[language].modal_label_origin,
                        modal_label_picture_path: text[language].modal_label_picture_path,
                        modal_button_cancel: text[language].modal_button_cancel,
                        modal_button_add_article: text[language].modal_button_add_article,
                        modal_button_update_article: text[language].modal_button_update_article,
                    }}
                />
            </div>
        </div>
    );
};

export default AdminSection;