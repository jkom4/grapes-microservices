import Article from "../Articles";

export interface ArticleModalProps {
    isOpen: boolean;
    editingArticle: Article | null;
    formData: Partial<Article>;
    errorMessage: string;
    onClose: () => void;
    onSubmit: (e: React.FormEvent) => void;
    onInputChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
    translations: {
        modal_add_article_title: string;
        modal_edit_article_title: string;
        modal_label_name: string;
        modal_label_description: string;
        modal_label_category_id: string;
        modal_label_family_id: string;
        modal_label_price_kg: string;
        modal_label_price_unit: string;
        modal_label_stock_kg: string;
        modal_label_stock_unit: string;
        modal_label_origin: string;
        modal_label_picture_path: string;
        modal_button_cancel: string;
        modal_button_add_article: string;
        modal_button_update_article: string;
    };
}