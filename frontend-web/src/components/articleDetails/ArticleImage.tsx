import Article from "../../utils/models/Articles";

interface ArticleImageProps {
    article: Article;
}

const ArticleImage: React.FC<ArticleImageProps> = ({ article }) => {
    return (
        <img
            src={article.picturePath}
            alt={article.name}
            className="w-full h-auto rounded-xl shadow-lg object-contain mt-12 max-h-[500px]"
        />
    );
};

export default ArticleImage;