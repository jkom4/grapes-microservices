import React from 'react';
import './index.css';
import Navbar from './layouts/Navbar';
import MainPage from './pages/Home';
import { LanguageProvider } from './features/LanguageContext';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import AllArticles from "./pages/AllArticles";

const App: React.FC = () => {
    return (
        <LanguageProvider>
            <Router>
                <Navbar />
                <Routes>
                    <Route path="/" element={<MainPage />} />
                    <Route path="/all-articles" element={<AllArticles />} />
                </Routes>
            </Router>
        </LanguageProvider>
    );
};

export default App;
