import React from 'react';
import Header from '../components/Header';
import IntroSection from '../components/IntroSection';
import LoginBox from '../components/LoginBox';
import Footer from '../components/Footer';
import email from '../styles/images/email.png';
import phone from '../styles/images/phone.jpg';
import eid from '../styles/images/eid_card.png';

const Home = () => {
    return (
        <div>
            <Header />
            <IntroSection />
            <div className="container">
                <LoginBox imgSrc={eid} altText="eID Card" loginMethod="eID card reader" description="Please connect your card reader before"/>
                <LoginBox imgSrc={email} altText="Email" loginMethod="Security code by email" description="You will receive a one-time password by mail"/>
                <LoginBox imgSrc={phone} altText="Phone" loginMethod="Security code by phone" description="You will receive a one-time password by phone"/>
            </div>
            <Footer />
        </div>
    );
};

export default Home;
