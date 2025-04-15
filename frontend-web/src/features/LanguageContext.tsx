// src/LanguageContext.tsx

import React, { createContext, useState, useContext, ReactNode } from 'react';

// Define the supported languages in the app
type Language = 'en' | 'fr'; // Only English (en) and French (fr) are supported

// Interface for the context type, containing both the current language and the method to update it
interface LanguageContextType {
    language: Language;
    setLanguage: (lang: Language) => void;
}

// Create the context with an initial value of undefined (it will be filled later by the provider)
const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

// Custom hook to access the language context value. Throws an error if used outside of the provider.
export const useLanguage = (): LanguageContextType => {
    const context = useContext(LanguageContext);
    if (!context) {
        throw new Error("useLanguage must be used within a LanguageProvider");
    }
    return context;
};

// Interface for the LanguageProvider props, expecting a children element (to wrap the app)
interface LanguageProviderProps {
    children: ReactNode;
}

// The LanguageProvider component that wraps the app and provides the language context to the entire app
export const LanguageProvider: React.FC<LanguageProviderProps> = ({ children }) => {
    // Default language is set to English ('en')
    const [language, setLanguage] = useState<Language>('en');

    // Function to update the language
    const changeLanguage = (lang: Language) => {
        setLanguage(lang); // Set the new language
        // You could add additional logic here for language handling, e.g., using libraries like i18next
    };

    // Providing the current language and the function to change the language to the rest of the app
    return (
        <LanguageContext.Provider value={{ language, setLanguage: changeLanguage }}>
            {children} {/* Render children (the app's components) with access to the language context */}
        </LanguageContext.Provider>
    );
};
