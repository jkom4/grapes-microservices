import React from 'react';

const Footer = () => {
    return (
        <footer className="bg-gray-900 text-gray-200 pt-12 pb-8 px-6 md:px-16 mt-16">
            <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-8">
                {/* Logo & Description */}
                <div>
                    <h2 className="text-xl font-bold text-white mb-2">MASI AUTH</h2>
                    <p className="text-sm text-gray-400">
                        A secure authentication service built as part of the Integrated Project (Group 4) within the MASI program.
                    </p>
                </div>

                {/* Navigation */}
                <div>
                    <h3 className="text-sm font-semibold text-white mb-3">Navigation</h3>
                    <ul className="space-y-2 text-sm">
                        <li><a href="/" className="hover:text-white">Home</a></li>
                        <li><a href="/legal-notice" className="hover:text-white">Terms of Use</a></li>
                        <li><a href="/privacy-policy" className="hover:text-white">Privacy Policy</a></li>
                        <li><a href="/id-reader" className="hover:text-white">How to Use the ID Reader</a></li>
                    </ul>
                </div>

                {/* Contact */}
                <div>
                    <h3 className="text-sm font-semibold text-white mb-3">Contact</h3>
                    <ul className="space-y-2 text-sm text-gray-400">
                        <li>HEPL - Campus Gloesener</li>
                        <li>Quai Gloesener 6</li>
                        <li>4020 Liège, Belgium</li>
                    </ul>
                </div>

                {/* Microservice Info */}
                <div>
                    <h3 className="text-sm font-semibold text-white mb-3">Integration</h3>
                    <p className="text-sm text-gray-400">
                        MASI AUTH is a modular microservice designed to integrate seamlessly with third-party applications.
                        It supports flexible authentication mechanisms and can be deployed independently or as part of a larger secure system.
                    </p>
                </div>
            </div>

            <div className="mt-10 border-t border-gray-700 pt-6 text-center text-sm text-gray-500">
                © 2025 MASI — All rights reserved.
            </div>
        </footer>
    );
};

export default Footer;
