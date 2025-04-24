import React, { useState } from "react";
import { useLanguage } from "../../features/LanguageContext";
import { deliveryAPI } from "../../services/httpCommon";
import {translationsAccount} from "../../utils/translations-account";


const DeliveryStatus: React.FC = () => {
    const { language } = useLanguage();
    const [orderId, setOrderId] = useState<string>("");
    const [status, setStatus] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(false);


    // Handle form submission
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setStatus(null);
        setLoading(true);

        // Validate input
        const orderIdNum = Number(orderId);
        if (!orderId || isNaN(orderIdNum) || orderIdNum <= 0) {
            setError(translationsAccount[language].invalidInput);
            setLoading(false);
            return;
        }

        try {
            const response = await fetch(
                `${deliveryAPI.baseURL}${deliveryAPI.endpoints.getDeliveryStatus(orderId)}`,
                {
                    method: "GET",
                }
            );

            if (!response.ok) {
                if (response.status === 400) {
                    throw new Error("Invalid Order ID");
                }
                throw new Error("Failed to fetch delivery status");
            }

            const data = await response.text();
            setStatus(data);
        } catch (err: any) {
            setError(err.message === "Invalid Order ID" ? translationsAccount[language].invalidInput : translationsAccount[language].errorDelivery);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-gradient-to-br from-gray-50 to-gray-100 p-8 rounded-xl shadow-2xl max-w-3xl mx-auto mt-6">
            <h2 className="text-3xl font-bold text-gray-800 mb-6 text-center">{translationsAccount[language].title}</h2>
            <form onSubmit={handleSubmit} className="space-y-6">
                <div className="relative">
                    <input
                        type="text"
                        value={orderId}
                        onChange={(e) => setOrderId(e.target.value)}
                        placeholder={translationsAccount[language].placeholder}
                        className="w-full p-4 pr-12 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent shadow-sm transition duration-200"
                        disabled={loading}
                    />
                    <svg
                        className="absolute right-4 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                        />
                    </svg>
                </div>
                <button
                    type="submit"
                    disabled={loading}
                    className="w-full p-4 bg-primary text-white rounded-lg hover:bg-accent disabled:bg-gray-400 disabled:cursor-not-allowed font-semibold transition duration-200 flex items-center justify-center space-x-2"
                >
                    {loading ? (
                        <svg
                            className="animate-spin h-5 w-5 text-white"
                            fill="none"
                            viewBox="0 0 24 24"
                        >
                            <circle
                                className="opacity-25"
                                cx="12"
                                cy="12"
                                r="10"
                                stroke="currentColor"
                                strokeWidth="4"
                            />
                            <path
                                className="opacity-75"
                                fill="currentColor"
                                d="M4 12a8 8 0 018-8v8h8a8 8 0 01-8 8 8 8 0 01-8-8z"
                            />
                        </svg>
                    ) : (
                        <span>{translationsAccount[language].submit}</span>
                    )}
                </button>
            </form>

            {error && (
                <p className="mt-4 text-red-600 bg-red-50 p-3 rounded-lg text-center">{error}</p>
            )}

            {status ? (
                <div className="mt-6 p-6 bg-white rounded-lg shadow-md">
                    <h3 className="text-xl font-semibold text-gray-800 mb-2">{translationsAccount[language].statusDelivery}</h3>
                    <p className="text-gray-600 text-lg">{status}</p>
                </div>
            ) : (
                !error && !loading && (
                    <p className="mt-4 text-gray-500 text-center">{translationsAccount[language].noStatus}</p>
                )
            )}
        </div>
    );
};

export default DeliveryStatus;