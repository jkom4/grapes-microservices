import React from 'react';

const IntroSection = () => {
    return (
        <section className="max-w-full bg-white p-8 sm:px-12 lg:px-24 space-y-8 shadow-lg rounded-lg">
            <div className="text-center">
                <h2 className="text-3xl font-bold text-gray-800 mb-4">Manage Your Digital Keys</h2>
                <p className="text-lg text-gray-700 mb-6">
                    MASI AUTH is a modular microservice designed to integrate seamlessly with third-party applications. It supports flexible authentication mechanisms and can be deployed independently or as part of a larger secure system.
                </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                <div className="flex flex-col items-center p-6 border border-gray-200 rounded-lg shadow-md bg-gray-50">
                    <h3 className="text-xl font-semibold text-gray-800 mb-3">eID Login</h3>
                    <p className="text-base text-gray-600 mb-4">Logging into the MASI with your eID offers the highest level of security.</p>
                    <p className="text-sm text-gray-500">It’s the most reliable and secure authentication method, ensuring the protection of your digital keys.</p>
                </div>

                <div className="flex flex-col items-center p-6 border border-gray-200 rounded-lg shadow-md bg-gray-50">
                    <h3 className="text-xl font-semibold text-gray-800 mb-3">Digital Key Benefits</h3>
                    <p className="text-base text-gray-600 mb-4">Digital keys provide simplicity, unique identification, recognizability, and security.</p>
                    <p className="text-sm text-gray-500">Experience the best in privacy, reliability, and convenience when using digital keys for authentication.</p>
                </div>

                <div className="flex flex-col items-center p-6 border border-gray-200 rounded-lg shadow-md bg-gray-50">
                    <h3 className="text-xl font-semibold text-gray-800 mb-3">Why Digital Keys?</h3>
                    <p className="text-base text-gray-600 mb-4">With digital keys, you enjoy more privacy and control over your online presence.</p>
                    <p className="text-sm text-gray-500">Secure, easy, and reliable – digital keys redefine how we interact with the digital world.</p>
                </div>
            </div>
        </section>
    );
};

export default IntroSection;
