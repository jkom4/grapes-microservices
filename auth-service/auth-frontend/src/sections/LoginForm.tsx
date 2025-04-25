import React from 'react';
import Loader from "../components/Loader";
import {AuthMethod} from "../models/User";

interface LoginFormProps {
    method: AuthMethod;
    loading: boolean;
    email: string;
    password: string;
    emailError: string;
    passwordError: string;
    pinCode: string[];
    showChallenge: boolean;
    challenge: string[];
    setEmail: React.Dispatch<React.SetStateAction<string>>;
    setPassword: React.Dispatch<React.SetStateAction<string>>;
    handleLogin: () => void;
    handleGetChallenge: () => void;
    handleChange: (e: React.ChangeEvent<HTMLInputElement>, index: number, type: 'challenge' | 'pin') => void;
    handleKeyDown: (e: React.KeyboardEvent<HTMLInputElement>, index: number, type: 'challenge' | 'pin' | 'credentials') => void;
    handlePaste: (e: React.ClipboardEvent<HTMLInputElement>, type: 'challenge' | 'pin') => void;
    challengeCountdown: number | null;
}

const LoginForm: React.FC<LoginFormProps> = ({
                                                 method,
                                                 loading,
                                                 email,
                                                 password,
                                                 emailError,
                                                 passwordError,
                                                 pinCode,
                                                 showChallenge,
                                                 challenge,
                                                 setEmail,
                                                 setPassword,
                                                 handleLogin,
                                                 handleGetChallenge,
                                                 handleChange,
                                                 handleKeyDown,
                                                 handlePaste,
                                                 challengeCountdown
                                             }) => {

    return (
        <div className="flex flex-col justify-between">
            {loading && <Loader/>}
            <section className="flex-1 flex justify-center items-center p-8 bg-gray-50">
                <div className="max-w-md w-full bg-white rounded-lg shadow-xl p-8">
                    <div className="text-center mb-6">
                        <h2 className="text-3xl font-semibold text-gray-800">Log In</h2>
                        <p className="text-gray-500 mt-2">Access your account securely.</p>
                    </div>

                    {!showChallenge ? (
                        <div className="space-y-6">
                            <div>
                                <label htmlFor="email" className="text-sm text-gray-700">Email</label>
                                <input
                                    id="email"
                                    type="email"
                                    placeholder="Email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    onKeyDown={(e) => handleKeyDown(e, -1, 'credentials')}
                                    className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    required
                                />
                                {emailError && <p className="text-sm text-red-500 mt-1">{emailError}</p>}
                            </div>

                            <div>
                                <label htmlFor="password" className="text-sm text-gray-700">Password</label>
                                <input
                                    id="password"
                                    type="password"
                                    placeholder="Password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    onKeyDown={(e) => handleKeyDown(e, -1, 'credentials')}
                                    className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    required
                                />
                                {passwordError && <p className="text-sm text-red-500 mt-1">{passwordError}</p>}
                            </div>

                            <button
                                onClick={handleGetChallenge}
                                disabled={loading}
                                className={`w-full py-3 bg-blue-500 text-white font-semibold rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
                            >
                                Log In
                            </button>
                        </div>
                    ) : (
                        <div className="space-y-2">
                            <p className="text-sm text-gray-500 text-center mb-1">
                                {method === 'EID'
                                    ? 'Enter your PIN to continue with eID authentication.'
                                    : `Verify your ${method} box to get the challenge.`}
                            </p>
                            {challengeCountdown !== null && (
                                <p className="text-sm text-gray-500 text-center mt-0">
                                    Challenge valid for
                                    another {challengeCountdown} second{challengeCountdown > 1 ? 's' : ''}
                                </p>
                            )}
                            <div className="flex justify-center flex-col items-center">
                                <label htmlFor="challenge" className="text-lg text-gray-700 mt-4 mb-4">Enter Challenge Code</label>
                                <div className="flex justify-center space-x-2">
                                    {method !== 'EID' && [...Array(6)].map((_, index) => (
                                        <input
                                            key={index}
                                            id={`challenge-${index}`}
                                            type="text"
                                            inputMode="numeric"
                                            pattern="[0-9]*"
                                            maxLength={1}
                                            value={challenge[index] || ''}
                                            onChange={(e) => handleChange(e, index, 'challenge')}
                                            onKeyDown={(e) => handleKeyDown(e, index, 'challenge')}
                                            onPaste={(e) => handlePaste(e, 'challenge')}
                                            className="w-12 p-3 border border-gray-300 rounded-md text-center focus:outline-none focus:ring-2 focus:ring-blue-500"
                                            placeholder="_"
                                        />
                                    ))}
                                </div>

                                <label htmlFor="pinCode" className="text-lg text-gray-700 mt-4 mb-4">Enter PIN Code</label>
                                <div className="flex justify-center space-x-2">
                                    {[...Array(4)].map((_, index) => (
                                        <input
                                            key={index}
                                            id={`pin-${index}`}
                                            type="password"
                                            inputMode="numeric"
                                            pattern="[0-9]*"
                                            maxLength={1}
                                            value={pinCode[index] || ''}
                                            onChange={(e) => handleChange(e, index, 'pin')}
                                            onKeyDown={(e) => handleKeyDown(e, index, 'pin')}
                                            onPaste={(e) => handlePaste(e, 'pin')}
                                            className="w-12 p-3 border border-gray-300 rounded-md text-center focus:outline-none focus:ring-2 focus:ring-blue-500"
                                            placeholder="_"
                                        />
                                    ))}
                                </div>
                                <button
                                    className={`mt-4 w-full py-3 bg-blue-500 text-white font-semibold rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 
      ${challenge.length !== 6 || pinCode.length !== 4 ? 'bg-gray-400 cursor-not-allowed hover:bg-gray-400' : 'hover:bg-blue-600'}`}
                                    disabled={challenge.length !== 6 || pinCode.length !== 4}
                                    onClick={() => {
                                        if (pinCode.length === 4 && (method === 'EID' || challenge.length === 6)) {
                                            handleLogin();
                                        }
                                    }}
                                >
                                    Submit
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </section>
        </div>
    );
};

export default LoginForm;
