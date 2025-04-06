
import React, { lazy, Suspense } from 'react';

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

// Chargement paresseux (lazy) de vos pages
const LoginPage = lazy(() => import('./pages/LoginPage'));
const PaymentPage = lazy(() => import('./pages/PaymentPage'));
const PaymentVerificationPage = lazy(() => import('./pages/PaymentVerificationPage'));

function App() {
  return (
      <BrowserRouter>
        <Suspense fallback={<div>Chargement...</div>}>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/payment" element={<PaymentPage />} />
            <Route path="/verification" element={<PaymentVerificationPage />} />
            {/* Rediriger la racine vers /login ou une autre page par défaut */}
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
  );
}

export default App;
