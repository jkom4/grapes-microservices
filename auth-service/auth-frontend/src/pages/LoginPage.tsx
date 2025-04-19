import React, {useRef, useState} from 'react';
import {AuthMethod} from '../models/User';
import {useParams} from "react-router-dom";
import {useAuth} from "../context/AuthContext";
import {toast} from "react-toastify";
import AlreadyAuthenticated from "../components/AlreadyAuthenticated";
import LoginForm from "../sections/LoginForm";
import {handleGetChallenge, handleLogin} from "../services/authService";

const LoginPage = () => {
    const { method } = useParams<{ method: AuthMethod }>();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [challenge, setChallenge] = useState<string[]>(Array(6).fill(''));
    const [pinCode, setPinCode] = useState<string[]>(Array(4).fill(''));
    const [emailError, setEmailError] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [loading, setLoading] = useState(false);
    const [showChallenge, setShowChallenge] = useState(false);
    const [challengeCountdown, setChallengeCountdown] = useState<number | null>(null);
    const intervalRef = useRef<NodeJS.Timeout | null>(null);

    if (!method) {
        throw new Error("Method is not provided in the URL");
    }

    const { isAuthenticated } = useAuth();

    if (isAuthenticated) {
        return <AlreadyAuthenticated />;
    }

    const triggerChallenge = () => {
        setShowChallenge(true);
        if (challenge.every(c => c === '')) {
            setChallenge(Array(6).fill(''));
        }
        setChallengeCountdown(60);

        if (intervalRef.current) clearInterval(intervalRef.current);

        intervalRef.current = setInterval(() => {
            setChallengeCountdown(prev => {
                if (prev && prev > 1) return prev - 1;

                clearInterval(intervalRef.current!);
                setShowChallenge(false);
                return null;
            });
        }, 1000);
    };

    const onClickGetChallenge = async () => {
        setLoading(true);
        setEmailError('');
        setPasswordError('');

        if (!email || !password) {
            if (!email) setEmailError('Email is required');
            if (!password) setPasswordError('Password is required');
            setLoading(false);
            return;
        }

        try {
            const { message } = await handleGetChallenge(email, password, method as AuthMethod);
            toast.success(message, {autoClose: 2000});
            triggerChallenge();
        } catch (err: any) {
            toast.error(err.message, {autoClose: 2000});
        } finally {
            setLoading(false);
            setChallenge(Array(6).fill(''));
            setPinCode(Array(4).fill(''));
        }
    };

    const onClickLogin = async () => {
        if (challenge.join('').length !== 6 || pinCode.join('').length !== 4) {
            toast.error('Please complete both the challenge and PIN code.', { autoClose: 2000 });
            return;
        }

        try {
            setLoading(true);
            const token = await handleLogin(
                challenge.join(''),
                pinCode.join(''),
                email,
                method
            );

            localStorage.setItem('jwt', token);
            toast.success('Login successful.', { autoClose: 2000 });

            window.location.href =
                new URLSearchParams(window.location.search).get('redirectUrl') || '/';

        } catch (err: any) {
            toast.error(err, { autoClose: 2000 });
        } finally {
            setLoading(false);
            setChallenge(Array(6).fill(''));
            setPinCode(Array(4).fill(''));
        }
    };

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement>,
        index: number,
        type: 'challenge' | 'pin'
    ) => {
        const value = e.target.value;

        if (type === 'challenge') {
            const newChallenge = [...challenge];
            newChallenge[index] = value;
            setChallenge(newChallenge);
            if (value && index < 5) {
                document.getElementById(`challenge-${index + 1}`)?.focus();
            }
        } else {
            const newPinCode = [...pinCode];
            newPinCode[index] = value;
            setPinCode(newPinCode);
            if (value && index < 3) {
                document.getElementById(`pin-${index + 1}`)?.focus();
            }
        }
    };


    const handleKeyDown = (
        e: React.KeyboardEvent<HTMLInputElement>,
        index: number,
        type: 'challenge' | 'pin'
    ) => {
        const values = type === 'challenge' ? challenge : pinCode;
        const prefix = type === 'challenge' ? 'challenge' : 'pin';

        if (e.key === 'Backspace' && values[index] === '') {
            if (index > 0) {
                document.getElementById(`${prefix}-${index - 1}`)?.focus();
            }
        }
    };

    const handlePaste = (
        e: React.ClipboardEvent<HTMLInputElement>,
        type: 'challenge' | 'pin'
    ) => {
        const pastedText = e.clipboardData.getData('Text');
        e.preventDefault();

        if (type === 'challenge' && /^[0-9]{6}$/.test(pastedText)) {
            setChallenge(pastedText.split(''));
        }

        if (type === 'pin' && /^[0-9]{4}$/.test(pastedText)) {
            setPinCode(pastedText.split(''));
        }
    };

    return (
        <LoginForm
            loading={loading}
            email={email}
            password={password}
            emailError={emailError}
            passwordError={passwordError}
            showChallenge={showChallenge}
            challenge={challenge}
            pinCode={pinCode}
            setEmail={setEmail}
            setPassword={setPassword}
            handleGetChallenge={onClickGetChallenge}
            handleLogin={onClickLogin}
            handleChange={handleChange}
            handleKeyDown={handleKeyDown}
            handlePaste={handlePaste}
            challengeCountdown={challengeCountdown}
        />
    );
};

export default LoginPage;
