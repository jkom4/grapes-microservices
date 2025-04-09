import React from 'react';
import './index.css';
import Navbar from "./layouts/Navbar";
import HomePage from "./pages/Home";
import MainPage from "./pages/Home";
import {LanguageProvider} from "./features/LanguageContext";

const App: React.FC = () => {
    return (
        <>
            <LanguageProvider>
            <Navbar />
            <MainPage />
            </LanguageProvider>
        </>
    );
};

export default App;