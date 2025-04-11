// src/components/Header.tsx
import React from 'react';
import { logout } from '../api/AuthApi';

const Header = () => {
    const handleLogout = async () => {
        const token = localStorage.getItem('jwt');
        if (!token) return;
        try {
            await logout(token);
            localStorage.removeItem('jwt');
            alert('Logged out');
        } catch (err) {
            alert('Logout failed.');
        }
    };

    return (
        <header>
            <h1>MASI AUTHENTICATION SERVICE</h1>
            <div className="buttons">
                <button className="logout-btn" onClick={handleLogout}>
                    Log Out
                </button>
                <button className="register-btn" onClick={() => window.location.href='/auth/register'}>
                    Register
                </button>
            </div>
        </header>
    );
};

export default Header;
