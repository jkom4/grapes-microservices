// src/components/IntroSection.tsx
import React, { useState } from 'react';
import { login, verifyChallenge } from '../api/AuthApi';
import { AuthMethod } from '../models/User';
const IntroSection = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [method, setMethod] = useState<AuthMethod>('EMAIL');
    const [showChallenge, setShowChallenge] = useState(false);
    const [challenge, setChallenge] = useState('');

    const handleLogin = async () => {
        try {
            await login(email, password, method);
            setShowChallenge(true);
        } catch (err) {
            alert('Login failed.');
        }
    };

    const handleVerify = async () => {
        try {
            const token = await verifyChallenge(parseInt(challenge), email);
            localStorage.setItem('jwt', token);
            alert('Login successful.');
            setShowChallenge(false);
        } catch (err) {
            alert('Challenge verification failed.');
        }
    };

    return (
        <section className="intro">
            <p>Manage your digital keys more easily yourself. Log in to activate or adjust your digital keys.</p><br />
            <p>Logging into the MASI with your eID is the standard method and offers the highest level of security...</p><br />
            <p>What are digital keys? Simplicity, Unique identification, Recognizability, Reliability, Privacy, Security...</p>
            <div>
                <input type="email" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
                <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
                <select value={method} onChange={e => setMethod(e.target.value as AuthMethod)}>
                    <option value="EMAIL">EMAIL</option>
                    <option value="MASI_ID">PHONE</option>
                    <option value="MASI_ID">EID</option>
                </select>
                <button onClick={handleLogin}>Log In</button>
            </div>
            {showChallenge && (
                <div className="popup">
                    <p>Enter Challenge Code</p>
                    <input type="text" maxLength={6} value={challenge} onChange={e => setChallenge(e.target.value)} />
                    <button onClick={handleVerify}>Submit</button>
                </div>
            )}
        </section>
    );
};

export default IntroSection;
