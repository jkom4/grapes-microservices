import React from 'react';

const IDReaderPage = () => {
    return (
        <div className="flex flex-col min-h-screen">
            <div className="flex-grow max-w-7xl mx-auto p-6">
                <h1 className="text-3xl font-bold mb-6">How to Use the Belgian ID Card Reader</h1>
                <p className="text-lg mb-4">To use the ID card reader, you will need to download and install the required software specifically for Belgian ID cards.</p>
                <p className="font-semibold text-red-600 mb-4">Please note: This reader is designed exclusively for Belgian identity cards.</p>
                <p className="text-lg mb-4">Follow the link below to download the necessary software for the Belgian eID reader:</p>
                <a
                    href="https://eid.belgium.be/fr"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-blue-500 hover:text-blue-700 text-lg"
                >
                    Click here to download the software.
                </a>
            </div>
        </div>
    );
};

export default IDReaderPage;
