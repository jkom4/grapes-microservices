import React, { useEffect, useState } from "react";
import "./index.css";
import Navbar from "./layouts/Navbar";
import MainPage from "./pages/Home";
import { LanguageProvider } from "./features/LanguageContext";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import AllArticles from "./pages/AllArticles";
import { ErrorDisplay } from "./components/AppErrorComponent";
import { jwtDecode } from "jwt-decode";
import { AuthProvider, useAuth } from "./features/AuthContext";
import { CartProvider } from "./features/CartContext";
import ArticleDetails from "./pages/ArticleDetails";
import AccountPage from "./pages/AccountPage";
import AdminPage from "./pages/admin/Admin";
import { toast, ToastContainer } from "react-toastify";
import ShoppingCart from "./pages/ShoppingCart";

interface JwtPayload {
    sub?: string;
    role?: string;
    name?: string;
    [key: string]: any;
}

const App: React.FC = () => {
    const params = new URLSearchParams(window.location.search);
    const accessTokenFromUrl = params.get("accessToken");
    const refreshToken = params.get("refreshToken");
    const state = params.get("state");

    const accessToken = accessTokenFromUrl || sessionStorage.getItem("accessToken");

    if (accessTokenFromUrl) {
        sessionStorage.setItem("accessToken", accessTokenFromUrl);
    }

    if (refreshToken) {
        sessionStorage.setItem("refreshToken", refreshToken);
    }

    if (state) {
        sessionStorage.setItem("state", state);
    }

    if (accessTokenFromUrl || refreshToken || state) {
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    const AppContent: React.FC = () => {
        const { stateAuthentication, setAuthData } = useAuth();
        const [error, setError] = useState<string | null>(null);

        useEffect(() => {
            if (accessToken) {
                try {
                    const decoded: JwtPayload = jwtDecode<JwtPayload>(accessToken);
                    const { sub, role, name } = decoded;
                    if (!sub) {
                        console.warn('No "sub" field found in JWT payload');
                    }
                    setAuthData(sub || null, role || null, name || null);
                } catch (error) {
                    console.error("Failed to decode JWT:", error);
                    toast.error("Invalid access token. Please try again.", {
                        position: "top-right",
                        autoClose: 5000,
                    });
                }
            } else {
            }

            if (state && stateAuthentication !== null) {
                if (parseInt(state) !== stateAuthentication) {
                    console.warn("State mismatch, removing accessToken");
                    sessionStorage.removeItem("accessToken");
                    setError("Authentication failed: Invalid state parameter.");
                    toast.error("Authentication failed. Please try again.", {
                        position: "top-right",
                        autoClose: 5000,
                        hideProgressBar: false,
                        closeOnClick: true,
                        pauseOnHover: true,
                        draggable: true,
                    });
                }
            }
        }, [stateAuthentication, state, setAuthData, accessToken]);

        const handleRetry = () => {
            setError(null);
            window.location.reload();
        };

        if (error) {
            return <ErrorDisplay message={error} onRetry={handleRetry} />;
        }

        return (
            <>
                <Navbar />
                <Routes>
                    <Route path="/" element={<MainPage />} />
                    <Route path="/all-articles" element={<AllArticles />} />
                    <Route path="/clm/articles/:id" element={<ArticleDetails />} />
                    <Route path="/shopping-cart" element={<ShoppingCart />} />
                    <Route path="/account" element={<AccountPage />} />
                    <Route path="/admin" element={<AdminPage />} />
                </Routes>
                <ToastContainer
                    position="top-right"
                    autoClose={3000}
                    hideProgressBar={false}
                    newestOnTop
                    closeOnClick
                    pauseOnFocusLoss
                    draggable
                    pauseOnHover
                />
            </>
        );
    };

    return (
        <Router>
            <LanguageProvider>
                <AuthProvider>
                    <CartProvider>
                        <AppContent />
                    </CartProvider>
                </AuthProvider>
            </LanguageProvider>
        </Router>
    );
};

export default App;