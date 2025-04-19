import React from 'react';
import { useNavigate } from 'react-router-dom';

interface LoginBoxProps {
    imgSrc: string;
    altText: string;
    title: string;
    loginMethod: string;
    description: string;
}

const LoginMethodBox: React.FC<LoginBoxProps> = ({ imgSrc, altText, title, loginMethod, description }) => {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(`/login/${loginMethod}`);
    };

    return (
        <div
            className={`bg-white p-6 rounded-xl shadow-lg transform transition-all hover:scale-105 hover:shadow-2xl duration-300`}
        >
            <img src={imgSrc} alt={altText} className="w-24 h-24 mb-4 mx-auto object-contain" />
            <h5 className="text-xl font-semibold text-gray-800 mb-2">{title}</h5>
            <p className="text-sm text-gray-600 mb-4">{description}</p>
            <button className="bg-blue-600 text-white py-2 px-6 rounded-lg hover:bg-blue-700 transition-colors duration-300" onClick={handleClick}>
                Login with {loginMethod}
            </button>
        </div>
    );
};

export default LoginMethodBox;
