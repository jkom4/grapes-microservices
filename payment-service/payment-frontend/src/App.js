import React, { lazy, Suspense } from 'react';
// Importing React along with 'lazy' for dynamic imports and 'Suspense' to handle loading states

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
// Importing routing tools: 'BrowserRouter' for browser history, 'Routes' for route definitions, 
// 'Route' for specific paths, and 'Navigate' for redirection

// Lazy loading page components to optimize performance by loading them only when needed
const LoginPage = lazy(() => import('./pages/LoginPage'));
// Dynamically imports LoginPage when the route is accessed
const PaymentPage = lazy(() => import('./pages/PaymentPage'));
// Loads PaymentPage on demand
const PaymentVerificationPage = lazy(() => import('./pages/PaymentVerificationPage'));
// Loads PaymentVerificationPage only when required

function App() {
    return (
        // BrowserRouter enables client-side routing using the browser's history API
        <BrowserRouter>
            <Suspense fallback={<div>Loading...</div>}>
                <Routes>
                    {/*// Defines a route for the login page at "/login"*/}
                    <Route path="/login" element={<LoginPage />} />
                    {/*// Route for the payment page, accessible at "/payment"*/}
                    <Route path="/payment" element={<PaymentPage />} />
                    {/*// Route for payment verification, available at "/verification"*/}
                    <Route path="/verification" element={<PaymentVerificationPage />} />
                    {/*// Catch-all route: redirects any unmatched path to "/login" using Navigate*/}
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </Suspense>
        </BrowserRouter>
    );
}

export default App;
