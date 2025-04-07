import './global.css';
import React, { lazy, Suspense } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';

// Lazy loaded components for code splitting
const LoginPage = lazy(() => import('./pages/LoginPage'));
const PaymentPage = lazy(() => import('./pages/PaymentPage'));
const PaymentVerificationPage = lazy(() => import('./pages/PaymentVerificationPage'));

function App() {
    return (
        <Router>  {/*Main router container*/}
            <Suspense fallback={<div className="loading">Loading...</div>}>  {/*Shows loading while components download*/}
                <Routes>  {/*Contains all application routes*/}
                    <Route path="/" element={<LoginPage />} /> {/*Default route*/}
                    <Route path="/payment" element={<PaymentPage />} /> {/* Payment flow*/}
                    <Route path="/verification" element={<PaymentVerificationPage />} />  {/*Verification process*/}
                </Routes>
            </Suspense>
        </Router>
    );
}

export default App;  // Export for use in index.js