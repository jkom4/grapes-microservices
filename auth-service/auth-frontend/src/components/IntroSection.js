import React from 'react';
import { Link } from 'react-router-dom';

const IntroSection = () => {
    return (
        <section className="intro">
            <p>Manage your digital keys more easily yourself. Log in to activate or adjust your digital keys.</p><br />
            <p>Logging into the government with your eID is the standard method and offers the highest level of security...</p><br />
            <p>What are digital keys? Simplicity, Unique identification, Recognizability, Reliability, Privacy, Security...</p>
            <button className="learn-more-btn">
                <Link to="/learn-more">Learn More</Link>
            </button>
        </section>
    );
};

export default IntroSection;
