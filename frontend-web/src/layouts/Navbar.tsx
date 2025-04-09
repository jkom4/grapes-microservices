// src/components/Navbar.tsx

import React from 'react';
import { useLanguage } from '../features/LanguageContext';  // Import the custom hook to access the language
import englishFlag from '../assets/images/english-flag.png';
import frenchFlag from '../assets/images/french-flag.png';
import logo from '../assets/images/logo.png';

function Navbar() {
    const { language, setLanguage } = useLanguage();  // Access language and setLanguage function

    const handleLanguageChange = (lang: 'en' | 'fr') => {
        setLanguage(lang);  // Update language globally
    };

    return (
        <header className="flex justify-between items-center p-4 bg-primary">
            <a href="/">
                <div className="text-lg font-normal">
                    <img src={logo} alt="logo" className="h-auto w-auto" />
                </div>
            </a>
            <nav className="flex gap-8 flex-grow justify-center">
                <a href="#aboutus" className="text-black text-lg hover:text-accent">{language === 'en' ? 'About Us' : 'À propos'}</a>
                <a href="/products" className="text-black text-lg hover:text-accent">{language === 'en' ? 'Our Product' : 'Nos Produits'}</a>
                <a href="#" className="text-black text-2xl">
                    <span>🛒</span>
                </a>
            </nav>
            <div className="flex items-center">
                {/* Language switcher with flags */}
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
