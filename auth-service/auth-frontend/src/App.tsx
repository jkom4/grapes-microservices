import React, {useEffect} from "react";
import {BrowserRouter as Router, Route, Routes} from "react-router-dom";
import Home from './pages/Home';
import LoginPage from './pages/LoginPage'
import RegisterClassicPage from "./pages/RegisterClassicPage";
import DashboardPage from "./pages/DashboardPage";
import {AuthProvider} from "./context/AuthContext";
import {ToastContainer} from "react-toastify";
import AdminPage from "./pages/AdminPage";
import LegalNoticePage from './pages/LegalNoticePage';
import PrivacyPolicyPage from './pages/PrivacyPolicyPage';
import IDReaderPage from './pages/IDReaderPage';
import RegisterEidPage from "./pages/RegisterEidPage";
import RegisterChoicePage from "./pages/RegisterPageChoice";
import Layout from "./layouts/MainLayout";

const App: React.FC = () => {
    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const redirectUri = params.get('redirect_uri');
        const clientId = params.get('client_id');
        const state = params.get('state');

        if (redirectUri) {
            sessionStorage.setItem('redirect_uri', redirectUri);
        }

        if (clientId) {
            sessionStorage.setItem('client_id', clientId);
        }

        if (state) {
            sessionStorage.setItem('state', state);
        }
    }, []);

    return (
        <Router>
            <ToastContainer
                hideProgressBar={false}
                newestOnTop
                closeOnClick
                pauseOnFocusLoss
                draggable

                  
                pauseOnHover={false}
            />
            <AuthProvider>
                <Layout>
                <Routes>
                    <Route path="/" element={<Home/>}/>
                    <Route path="/login/:method" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegisterChoicePage/>}/>
                    <Route path="/register/classic" element={<RegisterClassicPage/>}/>
                    <Route path="/register/eid" element={<RegisterEidPage/>}/>
                    <Route path="/dashboard" element={<DashboardPage/>}/>
                    <Route path="/admin" element={<AdminPage />} />
                    <Route path="/legal-notice" element={<LegalNoticePage />} />
                    <Route path="/privacy-policy" element={<PrivacyPolicyPage />} />
                    <Route path="/id-reader" element={<IDReaderPage />} />
                </Routes>
                </Layout>
            </AuthProvider>
        </Router>
    );
};

export default App;
