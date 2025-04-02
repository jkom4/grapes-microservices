import React from 'react';
import './index.css';
import Navbar from "./layouts/Navbar";
import HomePage from "./pages/Home";
import MainPage from "./pages/Home";

const App: React.FC = () => {
    return (
        <>
            <Navbar />
            <MainPage />
        </>
    );
};

export default App;