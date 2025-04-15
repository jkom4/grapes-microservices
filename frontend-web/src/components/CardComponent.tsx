import React from "react";
import Article from "../utils/models/Articles";
import placeholder from "../assets/images/fruit.png";
import { useNavigate } from "react-router-dom";

interface CardProps {
    article: Article;
    handleAddToCart: (article: Article, event: React.MouseEvent<HTMLButtonElement>) => void;
}

const CardComponent: React.FC<CardProps> = ({ article, handleAddToCart }) => {
    const navigate = useNavigate();

    return (
        <div
            className="card bg-white p-5 w-full max-w-[300px] rounded-lg shadow-lg text-center transition-transform duration-300 ease-in-out hover:translate-y-[-5px] mx-auto"
        >
            <div
                className="relative cursor-pointer"
                onClick={() => navigate(`/clm/articles/${article.id}`)}
            >
                <div className="absolute top-2 left-2">
          <span className="bg-white text-secondary text-sm font-semibold px-3 py-1 rounded-full">
            {article.rating} ★
          </span>
                </div>
                <img
                    src={article.picturePath || placeholder}
                    alt={article.name}
                    className="w-full h-auto rounded-lg mb-4"
                />
                <div className="absolute bottom-2 right-2 bg-secondary text-white text-sm font-semibold rounded-full px-3 py-1">
                    {article.priceKg} € / kg
                </div>
            </div>
            <div className="card-header flex justify-between items-center">
                <h3 className="text-lg font-semibold text-secondary">{article.name}</h3>
                <button
                    className="buy-btn bg-accent text-white w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm cursor-pointer hover:bg-[#D43F97] relative overflow-hidden transition-transform duration-200"
                    onClick={(e) => {
                        e.stopPropagation();
                        handleAddToCart(article, e);
                    }}
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                        className="w-5 h-5"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
                        />
                    </svg>
                    <span className="pulse-effect absolute inset-0 rounded-full bg-white opacity-0"></span>
                </button>
            </div>
        </div>
    );
};

export default CardComponent;