import React from 'react';

const PrivacyPolicyPage = () => {
    return (
        <div>
            <div className="max-w-7xl mx-auto p-6">
                <h1 className="text-3xl font-bold mb-6">Privacy Policy</h1>
                <p className="text-lg mb-4">This Privacy Policy outlines the types of personal information that is collected and recorded by MASI AUTH and how we use it. By using our services, you agree to the collection and use of information in accordance with this policy.</p>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">1. Information Collection</h2>
                    <p>We collect various types of information to provide and improve our service. The types of data we collect include:</p>
                    <ul className="list-disc pl-6 mt-2">
                        <li><strong>Personal Information:</strong> When you register or interact with our service, we may collect personal data such as your name, email address, and other necessary details.</li>
                        <li><strong>Usage Data:</strong> We may collect information about how our service is accessed and used, such as your device's IP address, browser type, and usage patterns.</li>
                        <li><strong>Cookies and Tracking Technologies:</strong> We use cookies to enhance your experience on our site. Cookies help us track your preferences and usage to improve the functionality of the service.</li>
                    </ul>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">2. Use of Information</h2>
                    <p>The information we collect is used for the following purposes:</p>
                    <ul className="list-disc pl-6 mt-2">
                        <li>To provide and maintain our service.</li>
                        <li>To notify you about changes to our service.</li>
                        <li>To allow you to participate in interactive features when you choose to do so.</li>
                        <li>To provide customer support and respond to your inquiries.</li>
                        <li>To monitor the usage of our service and detect any technical issues.</li>
                        <li>To send you promotional and marketing communications (if you have consented to receive them).</li>
                    </ul>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">3. Data Protection</h2>
                    <p>We are committed to securing your personal information. We implement a variety of security measures to ensure the safety of your personal data. However, please note that no method of transmission over the internet or electronic storage is 100% secure, and we cannot guarantee absolute security.</p>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">4. Sharing Your Information</h2>
                    <p>We do not sell or rent your personal data to third parties. We may share your information in the following circumstances:</p>
                    <ul className="list-disc pl-6 mt-2">
                        <li><strong>With Service Providers:</strong> We may share your data with third-party service providers to assist with the operation of our service, such as hosting and customer support.</li>
                        <li><strong>With Legal Authorities:</strong> We may disclose your personal information if required to do so by law or to comply with a legal process.</li>
                        <li><strong>With Consent:</strong> We may share your information with third parties if you have provided your explicit consent.</li>
                    </ul>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">5. Your Data Protection Rights</h2>
                    <p>You have the following rights regarding your personal data:</p>
                    <ul className="list-disc pl-6 mt-2">
                        <li><strong>Access:</strong> You have the right to access the personal data we hold about you.</li>
                        <li><strong>Rectification:</strong> You can request correction of any inaccurate or incomplete information we hold.</li>
                        <li><strong>Erasure:</strong> You can request the deletion of your personal data under certain conditions.</li>
                        <li><strong>Restriction of Processing:</strong> You can request the restriction of the processing of your personal data under certain conditions.</li>
                        <li><strong>Data Portability:</strong> You have the right to receive your data in a structured, commonly used format, and to transfer it to another service provider.</li>
                        <li><strong>Objection:</strong> You can object to the processing of your personal data under certain conditions.</li>
                    </ul>
                    <p>If you wish to exercise any of these rights, please contact us using <a href="mailto:support@masiauth.com" className="text-blue-500 hover:text-blue-700">support@masiauth.com</a>.</p>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">6. Changes to This Privacy Policy</h2>
                    <p>We may update our Privacy Policy from time to time. Any changes will be posted on this page with an updated effective date. We encourage you to review this Privacy Policy periodically for any updates or changes.</p>
                </section>

                <section>
                    <h2 className="text-2xl font-semibold mt-8 mb-4">7. Contact Us</h2>
                    <p>If you have any questions about this Privacy Policy or our data practices, please contact us:</p>
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

export default PrivacyPolicyPage;
