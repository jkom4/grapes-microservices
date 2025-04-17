import React from 'react';
import IntroSection from '../sections/IntroSection';
import LoginMethodBox from '../components/LoginMethodBox';
import email from '../styles/images/email.png';
import phone from '../styles/images/phone.jpg';
import eid from '../styles/images/eid_card.png';

const Home = () => {
    return (
        <div className="min-h-screen flex flex-col">
            <IntroSection />
            <div className="flex-1 bg-gray-50 py-10">
                <div className="max-w-6xl mx-auto px-4">
                    <h2 className="text-3xl font-bold text-center text-gray-800 mb-8">Choose your authentication method</h2>
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                        <LoginMethodBox
                            imgSrc={eid}
                            altText="eID Card"
                            title="eID card reader"
                            loginMethod="EID"
                            description="Please connect your card reader before"
                        />
                        <LoginMethodBox
                            imgSrc={email}
                            altText="Email"
                            title="Security code by email"
                            loginMethod="EMAIL"
                            description="You will receive a one-time password by mail"
                        />
                        <LoginMethodBox
                            imgSrc={phone}
                            altText="Phone"
                            title="Security code by phone"
                            loginMethod="SMS"
                            description="You will receive a one-time password by phone"
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Home;
