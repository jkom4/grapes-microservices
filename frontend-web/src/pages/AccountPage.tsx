import React, { useState } from "react";
import OrderHistory from "../sections/accountSection/AccountSection";
import { useLanguage } from "../features/LanguageContext";

// AccountPage Component: Displays a settings page with a collapsible sidebar and main content
const AccountPage: React.FC = () => {
    const { language } = useLanguage(); // State to manage language toggle
    const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false); // State to toggle sidebar

    // Text content in both languages (English and French)
    const text = {
        en: {
            settings: "Settings",
            orderHistory: "Order History",
            profile: "Profile",
            closeMenu: "Close Menu",
            openMenu: "Open Menu"
        },
        fr: {
            settings: "Paramètres",
            orderHistory: "Historique des commandes",
            profile: "Profil",
            closeMenu: "Fermer le menu",
            openMenu: "Ouvrir le menu"
        }
    };

    // Toggle sidebar visibility
    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen);
    };

    return (
        <div className="flex min-h-screen bg-gray-100">
            {/* Sidebar */}
            <div
                className={`fixed inset-y-0 left-0 z-50 bg-white shadow-lg transform ${
                    isSidebarOpen ? "translate-x-0" : "-translate-x-full"
                } md:relative md:translate-x-0 transition-transform duration-300 ease-in-out w-64`}
            >
                <div className="flex items-center justify-between p-4 border-b">
                    <h2 className="text-xl font-semibold text-gray-800">{text[language].settings}</h2>
                    <button
                        onClick={toggleSidebar}
                        className="md:hidden p-2 text-gray-600 hover:text-gray-800"
                        aria-label={isSidebarOpen ? text[language].closeMenu : text[language].openMenu}
                    >
                        {isSidebarOpen ? (
                            <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        ) : (
                            <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                            </svg>
                        )}
                    </button>
                </div>
                <nav className="p-4">
                    <ul className="space-y-2">
                        <li>
                            <button
                                className="w-full text-left px-4 py-2 text-gray-700 hover:bg-blue-100 hover:text-blue-600 rounded-md transition"
                                onClick={() => {
                                    /* Logique pour naviguer vers l'historique */
                                    setIsSidebarOpen(false); // Ferme le menu sur mobile après clic
                                }}
                            >
                                {text[language].orderHistory}
                            </button>
                        </li>
                        <li>
                            <button
                                className="w-full text-left px-4 py-2 text-gray-700 hover:bg-blue-100 hover:text-blue-600 rounded-md transition"
                                onClick={() => {
                                    /* Logique pour naviguer vers le profil */
                                    setIsSidebarOpen(false);
                                }}
                            >
                                {text[language].profile}
                            </button>
                        </li>
                        {/* Ajoutez d'autres liens ici */}
                    </ul>
                </nav>
            </div>

            {/* Main Content */}
            <div className="flex-1 p-6">
                {/* Toggle button for mobile */}
                <button
                    onClick={toggleSidebar}
                    className="md:hidden mb-4 p-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                    aria-label={isSidebarOpen ? text[language].closeMenu : text[language].openMenu}
                >
                    {isSidebarOpen ? (
                        <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    ) : (
                        <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                        </svg>
                    )}
                </button>
                {/* Content */}
                <OrderHistory />
            </div>

            {/* Overlay for mobile when sidebar is open */}
            {isSidebarOpen && (
                <div
                    className="fixed inset-0 bg-black bg-opacity-50 z-40 md:hidden"
                    onClick={toggleSidebar}
                ></div>
            )}
        </div>
    );
};

export default AccountPage;