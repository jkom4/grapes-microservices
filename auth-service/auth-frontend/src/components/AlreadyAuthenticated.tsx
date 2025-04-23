import React from 'react';
import { useNavigate } from 'react-router-dom';

const AlreadyAuthenticated = () => {
    const navigate = useNavigate();

    return (
        <div className="text-center text-lg mt-10">
            You are already logged in.
            <br/>
            <button
                onClick={() => navigate('/')}
                className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            >
                Go to Home
            </button>
        </div>
    );
};

export default AlreadyAuthenticated;
