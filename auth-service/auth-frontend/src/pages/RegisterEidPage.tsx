import React, {useEffect, useState} from 'react';
import { useNavigate } from 'react-router-dom';
import {useAuth} from "../context/AuthContext";
import AlreadyAuthenticated from "../components/AlreadyAuthenticated";
import {registerWithEid} from "../services/authService";
import { ErrorUtils } from "../utils/ErrorUtils";
import {toast} from "react-toastify";

const RegisterEidPage = () => {
    const [email, setEmail] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const {isAuthenticated} = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);

        try {
            await registerWithEid(email, password, phoneNumber);
            toast.success('Registration successful', { autoClose: 2000 });
            navigate('/');
        } catch (err: any) {
            ErrorUtils.handleErrors(err, setError);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            navigate('/dashboard');
        }
    }, [navigate]);

    if (isAuthenticated) {
        return <AlreadyAuthenticated />;
    }

    return (
        <div className="flex flex-col min-h-screen">
            <div className="flex-grow flex items-center justify-center p-6">
                <div className="max-w-md w-full bg-white p-8 rounded-lg shadow-lg">
                    <h1 className="text-3xl font-bold text-center mb-6">Register with Belgian ID Card</h1>
                    <p className="text-lg text-center mb-4 text-red-600 font-semibold">
                        Please note: This registration process is exclusively for Belgian identity cards.
                    </p>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div>
                            <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email Address</label>
                            <input
                                type="email"
                                id="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                className="mt-2 p-2 w-full border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Enter your email"
                            />
                        </div>

                        <div>
                            <label htmlFor="password" className="block text-sm font-medium text-gray-700">Password</label>
                            <input
                                type="password"
                                id="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                className="mt-2 p-2 w-full border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Enter your password"
                            />
                        </div>

                        <div>
                            <label htmlFor="phone" className="block text-sm font-medium text-gray-700">Phone Number</label>
                            <input
                                type="tel"
                                id="phone"
                                value={phoneNumber}
                                onChange={(e) => setPhoneNumber(e.target.value)}
                                required
                                className="mt-2 p-2 w-full border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Enter your phone number"
                            />
                        </div>

                        <div className="flex items-center justify-between mt-4">
                            <button
                                type="submit"
                                className="w-full bg-blue-500 text-white p-2 rounded-md shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                Register
                            </button>
                        </div>
                    </form>

                    <div className="mt-6 text-center">
                        <p className="text-sm text-gray-600">
                            Before registering, please ensure you have downloaded the required software for Belgian eID cards.
                        </p>
                        <a
                            href="/id-reader"
                            className="text-blue-500 hover:text-blue-700 text-lg mt-2 block"
                        >
                            Click here to download the software.
                        </a>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RegisterEidPage;
