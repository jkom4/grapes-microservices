import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import OrderHistory from "../sections/accountSection/AccountSection";
import { useLanguage } from "../features/LanguageContext";
import ProfileSection from "../sections/profileSection/ProfileSection";
import DeliveryStatus from "../sections/deliveryStatus/DeliveryStatus";
import { translationsAccount } from "../utils/translations-account";
import accountService from "../services/accountService";
import {authenticationService} from "../services/authenticationService"; // Import accountService

const AccountPage: React.FC = () => {
    const { language } = useLanguage();
    const navigate = useNavigate();
    const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false);
    const [activeSection, setActiveSection] = useState<
        "orderHistory" | "profile" | "deliveryStatus"
    >("orderHistory");
    const [logoutLoading, setLogoutLoading] = useState<boolean>(false); // Track logout loading
    const [logoutError, setLogoutError] = useState<string | null>(null); // Track logout errors

    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen);
    };

    const handleLogout = async () => {
        setLogoutLoading(true);
        setLogoutError(null);

        try {
            const accessToken = sessionStorage.getItem("accessToken");
            if (!accessToken) {
                throw new Error(translationsAccount[language].noTokenError || "No authentication token found");
            }

            // Call the logout API
            await authenticationService.logout(accessToken);

            // Clear storage
            sessionStorage.removeItem("accessToken");
            sessionStorage.removeItem("refreshToken");
            sessionStorage.removeItem("state");
            localStorage.removeItem("role");
            localStorage.removeItem("name");
            localStorage.removeItem("orderId");
            localStorage.removeItem("sub");

            // Provide feedback (optional: replace alert with a toast or other UI feedback)
            alert(translationsAccount[language].logoutSuccess || "You are logged out!");

            // Redirect to homepage
            window.location.href = "/";
        } catch (err) {
            const errorMessage =
                err instanceof Error
                    ? err.message
                    : translationsAccount[language].logoutError || "Logout failed";
            setLogoutError(errorMessage);
            setLogoutLoading(false);
        }
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
                    <h2 className="text-xl font-semibold text-gray-800">{translationsAccount[language].settings}</h2>
                    <button
                        onClick={toggleSidebar}
                        className="md:hidden p-2 text-gray-600 hover:text-gray-800"
                        aria-label={isSidebarOpen ? translationsAccount[language].closeMenu : translationsAccount[language].openMenu}
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
                                className="w-full text-left px-4 py-2 text-gray-700 hover:bg-primary hover:text-black rounded-md transition"
                                onClick={() => {
                                    setActiveSection("profile");
                                    setIsSidebarOpen(false);
                                }}
                            >
                                {translationsAccount[language].profile}
                            </button>
                        </li>
                        <li>
                            <button
                                className="w-full text-left px-4 py-2 text-gray-700 hover:bg-primary hover:text-black rounded-md transition"
                                onClick={() => {
                                    setActiveSection("orderHistory");
                                    setIsSidebarOpen(false);
                                }}
                            >
                                {translationsAccount[language].orderHistory}
                            </button>
                        </li>
                        <li>
                            <button
                                className="w-full text-left px-4 py-2 text-gray-700 hover:bg-primary hover:text-black rounded-md transition"
                                onClick={() => {
                                    setActiveSection("deliveryStatus");
                                    setIsSidebarOpen(false);
                                }}
                            >
                                {translationsAccount[language].deliveryStatus}
                            </button>
                        </li>
                        <li>
                            <button
                                onClick={handleLogout}
                                disabled={logoutLoading}
                                className={`w-full text-left px-4 py-2 rounded-md transition ${
                                    logoutLoading
                                        ? "text-gray-400 bg-gray-200 cursor-not-allowed"
                                        : "text-red-600 hover:bg-red-100"
                                }`}
                            >
                                {logoutLoading
                                    ? translationsAccount[language].loggingOut || "Logging out..."
                                    : translationsAccount[language].logout}
                            </button>
                        </li>
                    </ul>
                </nav>
            </div>

            {/* Main Content */}
            <div className="flex-1 p-6">
                {logoutError && (
                    <div className="mb-4 p-4 bg-red-100 text-red-700 rounded-md">
                        {logoutError}
                    </div>
                )}
                <button
                    onClick={toggleSidebar}
                    className="md:hidden mb-4 p-2 bg-primary text-white rounded-md hover:bg-accent"
                    aria-label={isSidebarOpen ? translationsAccount[language].closeMenu : translationsAccount[language].openMenu}
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

                {activeSection === "orderHistory" && <OrderHistory />}
                {activeSection === "profile" && <ProfileSection />}
                {activeSection === "deliveryStatus" && <DeliveryStatus />}
            </div>

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