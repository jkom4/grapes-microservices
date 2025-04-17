import Article from "../Articles";

export interface ArticleTableProps {
    articles: Article[];
    onEdit: (id: number) => void;
    onDelete: (id: number) => void;
    translations: {
        column_id: string;
        column_name: string;
        column_price_kg: string;
        column_price_unit: string;
        column_stock_kg: string;
        column_stock_unit: string;
        column_origin: string;
        column_actions: string;
    };
}