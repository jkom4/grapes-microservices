import React from 'react';

const Header = () => {
    return (
        <header>
            <h1>MASI AUTHENTICATION SERVICE</h1>
            <div className="buttons">
                <button className="logout-btn" onClick={() => window.location.href='/auth/logout'}>
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
