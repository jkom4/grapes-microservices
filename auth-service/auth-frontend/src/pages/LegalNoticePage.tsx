import React from 'react';

const LegalNoticePage = () => {
    return (
        <div>
            <div className="max-w-7xl mx-auto p-6">
                <h1 className="text-3xl font-bold mb-6">Legal Notice</h1>
                <p className="text-lg mb-4">This legal notice provides the terms and conditions governing the use of our website. By accessing or using this website, you agree to comply with and be bound by the following terms. If you do not agree with these terms, please refrain from using our website.</p>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">1. Company Information</h2>
                    <p><strong>Company Name:</strong> MASI AUTH</p>
                    <p><strong>Address:</strong> HEPL - Campus Gloesener, Quai Gloesener 6, 4020 Liège, Belgium</p>
                    <p><strong>Phone:</strong> +32 4 123 4567</p>
                    <p><strong>Email:</strong> <a href="mailto:support@masiauth.com" className="text-blue-500 hover:text-blue-700">support@masiauth.com</a></p>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">2. Website Terms of Use</h2>
                    <p>The content of this website is for general information purposes only. We make no representations or warranties of any kind, express or implied, about the completeness, accuracy, reliability, suitability, or availability of the website or the information, products, services, or related graphics contained on the website for any purpose. Any reliance you place on such information is therefore strictly at your own risk.</p>
                    <p>In no event shall we be liable for any loss or damage, including without limitation, indirect or consequential loss or damage, or any loss or damage whatsoever arising from loss of data or profits arising out of, or in connection with, the use of this website.</p>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">3. Intellectual Property</h2>
                    <p>All content on this website, including but not limited to text, graphics, logos, images, and software, is the property of MASI AUTH or its content providers and is protected by applicable copyright, trademark, and intellectual property laws. You may not copy, reproduce, distribute, or otherwise use the content without explicit written permission from MASI AUTH.</p>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">4. Links to Third-Party Websites</h2>
                    <p>Our website may contain links to external sites that are not operated by us. MASI AUTH has no control over the content, privacy policies, or practices of third-party websites, and assumes no responsibility for them. We recommend reviewing the privacy policies and terms of use of any third-party websites you visit.</p>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">5. Privacy and Data Protection</h2>
                    <p>We respect your privacy and are committed to protecting your personal data. Our Privacy Policy provides detailed information on how we collect, use, and safeguard your information. By using this website, you agree to the terms of our Privacy Policy. You can review the Privacy Policy <a href="/privacy-policy" className="text-blue-500 hover:text-blue-700">here</a>.</p>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">6. Governing Law</h2>
                    <p>These terms and conditions are governed by and construed in accordance with the laws of Belgium. Any dispute arising out of or in connection with these terms shall be subject to the exclusive jurisdiction of the courts of Liège, Belgium.</p>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">7. Changes to the Legal Notice</h2>
                    <p>We reserve the right to modify or update this legal notice at any time. Any changes will be posted on this page with an updated date. Please check this page regularly for any updates or modifications.</p>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">8. Contact Information</h2>
                    <p>If you have any questions about this legal notice, please contact us:</p>
                    <ul className="list-disc pl-6 mt-2">
                        <li>Email: <a href="mailto:support@masiauth.com" className="text-blue-500 hover:text-blue-700">support@masiauth.com</a></li>
                        <li>Phone: +32 4 123 4567</li>
                        <li>Address: HEPL - Campus Gloesener, Quai Gloesener 6, 4020 Liège, Belgium</li>
                    </ul>
                </section>
            </div>
        </div>
    );
};

export default LegalNoticePage;
