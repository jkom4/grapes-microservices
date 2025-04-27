import React from 'react';
import { useLanguage } from '../../features/LanguageContext';
import { Link } from 'react-router-dom';

const ProfileSection: React.FC = () => {
    const { language } = useLanguage();

    const text = {
        en: {
            title: "Profile",
            modifyInfo: "Edit my personal information",
        },
        fr: {
            title: "Profil",
            modifyInfo: "Modifier mes informations personnelles",
        }
    };

    return (
        <div className="container mx-auto p-6 bg-white rounded-lg shadow-md">
            <h2 className="text-2xl font-semibold text-gray-800 mb-4">
                {text[language].title}
            </h2>
            <Link
                to="#"
                className="text-accent hover:text-accent-dark font-semibold"
            >
                {text[language].modifyInfo}
            </Link>
        </div>
    );
};

export default ProfileSection;
