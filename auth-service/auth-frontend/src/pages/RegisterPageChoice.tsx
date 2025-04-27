import React from 'react';
import { Link } from 'react-router-dom';

const RegisterChoicePage: React.FC = () => {
    return (
        <div className="flex flex-col">
            <div className="flex-grow flex items-center justify-center py-12 px-6">
                <div className="max-w-5xl w-full bg-white p-10 rounded-lg shadow-lg border border-gray-200">
                    <h1 className="text-4xl font-extrabold text-center text-gray-800 mb-8">Choose Your Registration Method</h1>
                    <p className="text-lg text-center mb-12 text-gray-600">
                        You can register in two ways: by using your Belgian eID (Electronic Identity Card) or through a classic form. Choose the method that suits you best.
                    </p>

                    {/* Two options side by side */}
                    <div className="flex space-x-10">
                        {/* eID Option */}
                        <div className="flex-1 bg-blue-50 p-8 rounded-lg shadow-md hover:shadow-xl transition-all flex flex-col justify-between">
                            <div>
                                <h2 className="text-3xl font-semibold text-center text-blue-600 mb-6">Register with eID</h2>
                                <p className="text-center text-gray-700 mb-6">
                                    Using your Belgian eID allows for faster and more secure registration. You will need to have the eID software installed to proceed.
                                </p>
                            </div>
                            <Link
                                to="/register/eid"
                                className="mt-auto block text-center text-white bg-blue-600 hover:bg-blue-700 py-3 px-6 rounded-lg text-xl transition-all"
                            >
                                Register with eID
                            </Link>
                        </div>

                        {/* Classic Form Option */}
                        <div className="flex-1 bg-green-50 p-8 rounded-lg shadow-md hover:shadow-xl transition-all flex flex-col justify-between">
                            <div>
                                <h2 className="text-3xl font-semibold text-center text-green-600 mb-6">Register with Form</h2>
                                <p className="text-center text-gray-700 mb-6">
                                    If you prefer, you can create your account through a traditional form by entering your email, phone number, and password manually.
                                </p>
                            </div>
                            <Link
                                to="/register/classic"
                                className="mt-auto block text-center text-white bg-green-600 hover:bg-green-700 py-3 px-6 rounded-lg text-xl transition-all"
                            >
                                Register with Classic Form
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export {};

export default RegisterChoicePage;
