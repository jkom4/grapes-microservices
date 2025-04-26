import React from 'react';
import { Link } from 'react-router-dom';
import { useLanguage } from '../features/LanguageContext';
import { useAuth } from '../features/AuthContext';
import { generateLoginUrl } from '../services/httpCommon';
import englishFlag from '../assets/images/english-flag.png';
import frenchFlag from '../assets/images/french-flag.png';
import logo from '../assets/images/logo.png';

function Navbar() {
    const { language, setLanguage } = useLanguage();
    const { stateAuthentication, generateRandomNumber, sub, role } = useAuth();

    const isLoggedIn = sessionStorage.getItem('accessToken') !== null;
    const isAdmin = role === 'ADMIN';
    const isUser = role === 'USER';

    const handleLanguageChange = (lang: 'en' | 'fr') => {
        setLanguage(lang);
    };

    const handleLoginClick = () => {
        const randomNum = Math.floor(Math.random() * 1000);
        sessionStorage.setItem('stateAuthentication', randomNum.toString());
        generateRandomNumber();
        const loginUrl = generateLoginUrl(randomNum);
        window.open(loginUrl, 'noopener,noreferrer');
    };

    return (
        <header className="flex justify-between items-center p-4 bg-primary">
            <Link to="/">
                <div className="text-lg font-normal">
                    <img src={logo} alt="logo" className="h-auto w-auto" />
                </div>
            </Link>
            <nav className="flex gap-8 flex-grow justify-center items-center">
                <Link to="/" className="text-black hover:text-accent">
                    {language === 'en' ? 'Home' : 'Accueil'}
                </Link>
                <Link to="/all-articles" className="text-black hover:text-accent">
                    {language === 'en' ? 'Our Product' : 'Nos Produits'}
                </Link>
                {isLoggedIn && (
                    <Link to="/account" className="text-black hover:text-accent">
                        {language === 'en' ? 'Account' : 'Mon compte'}
                    </Link>
                )}
                <Link to="/shopping-cart" className="text-black hover:text-accent">
                    <span>🛒</span>
                </Link>
                {!isLoggedIn && (
                    <a
                        className="text-white text-lg bg-secondary px-4 py-1 rounded hover:bg-accent cursor-pointer"
                        onClick={handleLoginClick}
                    >
                        {language === 'en' ? 'Login' : 'Se connecter'}
                    </a>
                )}

                {isLoggedIn && isAdmin && (
                    <Link
                        to="/admin"
                        className="text-white text-lg bg-accent px-4 py-1 rounded hover:bg-secondary"
                    >
                        Admin
                    </Link>
                )}
            </nav>
            <div className="flex items-center">
                <a
                    href="../chat_exe/frontend_chat.exe"
                    download="frontend_chat.exe"
                    className="text-white text-lg bg-accent px-4 py-3 rounded-3xl hover:bg-secondary cursor-pointer mr-4"
                >
                    {language === 'en' ? 'Need help ?' : 'Besoin d\'aide ?'}
                </a>
                <button onClick={() => handleLanguageChange('en')} className="ml-4">
                    <img src={englishFlag} alt="English" className="h-6 w-6 rounded-full cursor-pointer" />
                </button>
                <button onClick={() => handleLanguageChange('fr')} className="ml-4">
                    <img src={frenchFlag} alt="Français" className="h-6 w-6 rounded-full cursor-pointer" />
                </button>
            </div>
        </header>
    );
}

export default Navbar;