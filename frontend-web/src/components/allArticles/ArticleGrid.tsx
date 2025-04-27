import Article from "../../utils/models/Articles";
import CardComponent from "../CardComponent";
import { translationsAllArticles } from "../../utils/translations-all-articles";

interface ArticleGridProps {
    articles: Article[];
    translations: typeof translationsAllArticles["en"];
    handleOpenModal: (article: Article, event: React.MouseEvent<HTMLButtonElement>) => void;
}

const ArticleGrid: React.FC<ArticleGridProps> = ({ articles, translations, handleOpenModal }) => {
    return (
        <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8 px-4 max-w-screen-lg mx-auto pb-20">
            {articles.length > 0 ? (
                articles.map((article: Article) => (
                    <CardComponent
                        key={article.id}
                        article={article}
                        handleAddToCart={handleOpenModal}
                    />
                ))
            ) : (
                <div className="col-span-full text-center py-8 text-gray-500">
                    <h3 className="text-xl font-semibold">{translations.noArticles}</h3>
                </div>
            )}
        </section>
    );
};

export default ArticleGrid;