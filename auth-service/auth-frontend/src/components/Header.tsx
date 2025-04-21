import React, {useState} from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { logout } from "../services/authService";
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';
import Loader from "./Loader";

const Header = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [loading, setLoading] = useState(false);
    const { isAuthenticated, role, token, setToken } = useAuth();

    const onLogoutClick = async () => {
        try {
            if (!token) {
                toast.warning("No active session found.", {autoClose: 2000});
                return;
            }

            await logout(token);
            toast.success("Successfully logged out.", {autoClose: 2000});

            await setToken(null);

            navigate("/");
        } catch (error: any) {
            toast.error(error.message || "Logout failed.", {autoClose: 2000});
        }
    };

    const handleNavigateToDashboard = () => {
        navigate('/dashboard');
    };

    return (
        <header className="bg-gray-900 text-white py-6 px-12 w-full shadow-lg">
            {loading && <Loader />}
            <div className="w-full flex justify-between items-center">
                <div className="flex items-center space-x-10">
                    <Link to="/" className="text-3xl font-semibold text-white hover:text-gray-300 transition duration-300">
                        MASI AUTH
                    </Link>
                    <span className="hidden md:inline text-xl font-medium text-gray-400">Authentication Service</span>
                </div>

                {/* Right Section: Buttons */}
                <div className="flex items-center space-x-8">
                    {isAuthenticated ? (
                        <>
                            {/* Admin Panel Button */}
                            {role === 'ADMIN' && location.pathname !== '/admin' && (
                                <button
                                    className="px-10 py-4 bg-yellow-600 text-white text-lg font-semibold rounded-lg shadow-md transition duration-300 transform hover:scale-105 hover:bg-yellow-700 focus:outline-none focus:ring-4 focus:ring-yellow-500"
                                    onClick={() => navigate('/admin')}
                                >
                                    Admin Panel
                                </button>
                            )}

                            {/* Dashboard Button */}
                            {location.pathname !== '/dashboard' && (
                                <button
                                    className="px-10 py-4 bg-blue-600 text-white text-lg font-semibold rounded-lg shadow-md transition duration-300 transform hover:scale-105 hover:bg-blue-700 focus:outline-none focus:ring-4 focus:ring-blue-500"
                                    onClick={handleNavigateToDashboard}
                                >
                                    Dashboard
                                </button>
                            )}

                            {/* Logout Button */}
                            <button
                                className="px-10 py-4 bg-red-600 text-white text-lg font-semibold rounded-lg shadow-md transition duration-300 transform hover:scale-105 hover:bg-red-700 focus:outline-none focus:ring-4 focus:ring-red-500"
                                onClick={onLogoutClick}
                            >
                                Log Out
                            </button>
                        </>
                    ) : (
                        <button
                            className="px-10 py-4 bg-green-600 text-white text-lg font-semibold rounded-lg shadow-md transition duration-300 transform hover:scale-105 hover:bg-green-700 focus:outline-none focus:ring-4 focus:ring-green-500"
                            onClick={() => navigate('/register')}
                        >
                            Register
                        </button>
                    )}
                </div>
            </div>
        </header>
    );
};

export default Header;
