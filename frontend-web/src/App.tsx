import React, { useEffect, useState } from 'react';
import './index.css';
import Navbar from './layouts/Navbar';
import MainPage from './pages/Home';
import { LanguageProvider } from './features/LanguageContext';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import AllArticles from './pages/AllArticles';
import { ErrorDisplay } from './components/AppErrorComponent';
import { jwtDecode } from 'jwt-decode';
import { AuthProvider, useAuth } from './features/AuthContext';
import { CartProvider } from './features/CartContext';
import ArticleDetails from './pages/ArticleDetails';
import AccountPage from './pages/AccountPage';
import AdminPage from './pages/admin/Admin';
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
    const accessTokenFromUrl = params.get('accessToken');
    const refreshToken = params.get('refreshToken');
    const state = params.get('state');

    console.log('URL Parameters:', { accessTokenFromUrl, refreshToken, state });

    const accessToken = accessTokenFromUrl || sessionStorage.getItem('accessToken');

    console.log('Selected accessToken:', accessToken);

    if (accessTokenFromUrl) {
        sessionStorage.setItem('accessToken', accessTokenFromUrl);
    }

    if (refreshToken) {
        sessionStorage.setItem('refreshToken', refreshToken);
    }

    if (state) {
        sessionStorage.setItem('state', state);
    }

    const AppContent: React.FC = () => {
        const { stateAuthentication, setAuthData } = useAuth();
        const [error, setError] = useState<string | null>(null);

        useEffect(() => {
            if (accessToken) {
                try {
                    console.log('Attempting to decode accessToken:', accessToken);
                    const decoded: JwtPayload = jwtDecode<JwtPayload>(accessToken);
                    console.log('Decoded JWT payload:', decoded);
                    const { sub, role, name } = decoded;
                    console.log('Extracted values:', { sub, role, name });
                    if (!sub) {
                        console.warn('No "sub" field found in JWT payload');
                    }
                    console.log('Calling setAuthData with:', { sub: sub || null, role: role || null, name: name || null });
                    setAuthData(sub || null, role || null, name || null);
                } catch (error) {
                    console.error('Failed to decode JWT:', error);
                    toast.error('Invalid access token. Please try again.', {
                        position: 'top-right',
                        autoClose: 5000,
                    });
                }
            } else {
                console.log('No accessToken available (neither in URL nor sessionStorage)');
            }

            if (state && stateAuthentication !== null) {
                console.log('Comparing state:', { urlState: state, stateAuthentication });
                if (parseInt(state) !== stateAuthentication) {
                    console.warn('State mismatch, removing accessToken');
                    sessionStorage.removeItem('accessToken');
                    setError('Authentication failed: Invalid state parameter.');
                    toast.error('Authentication failed. Please try again.', {
                        position: 'top-right',
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
            <Router>
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
            </Router>
        );
    };

    if (accessTokenFromUrl || refreshToken || state) {
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    return (
        <LanguageProvider>
            <AuthProvider>
                <CartProvider>
                    <AppContent />
                </CartProvider>
            </AuthProvider>
        </LanguageProvider>
    );
};

export default App;