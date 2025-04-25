import React, { createContext, useContext, useState, useEffect } from 'react';

interface AuthContextType {
    stateAuthentication: number | null;
    sub: string | null;
    role: string | null;
    name: string | null;
    generateRandomNumber: () => void;
    setAuthData: (sub: string | null, role: string | null, name: string | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

interface AuthProviderProps {
    children: React.ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const initialNumber = localStorage.getItem('stateAuthentication')
        ? parseInt(localStorage.getItem('stateAuthentication') as string)
        : null;

    const initialSub = localStorage.getItem('sub') || null;
    const initialRole = localStorage.getItem('role') || null;
    const initialName = localStorage.getItem('name') || null;

    const [stateAuthentication, setStateAuthentication] = useState<number | null>(initialNumber);
    const [sub, setSub] = useState<string | null>(initialSub);
    const [role, setRole] = useState<string | null>(initialRole);
    const [name, setName] = useState<string | null>(initialName);

    useEffect(() => {
        if (stateAuthentication !== null) {
            localStorage.setItem('stateAuthentication', stateAuthentication.toString());
        }
        if (sub !== null) {
            localStorage.setItem('sub', sub);
        } else {
            localStorage.removeItem('sub');
        }
        if (role !== null) {
            localStorage.setItem('role', role);
        } else {
            localStorage.removeItem('role');
        }
        if (name !== null) {
            localStorage.setItem('name', name);
        } else {
            localStorage.removeItem('name');
        }
    }, [stateAuthentication, sub, role, name]);

    useEffect(() => {
        const logoutTimer = setTimeout(() => {
            localStorage.clear();
            sessionStorage.clear();
        }, 15 * 60 * 1000);

        return () => clearTimeout(logoutTimer);
    }, []);
    const generateRandomNumber = () => {
        const array = new Uint32Array(1);
        window.crypto.getRandomValues(array);
        const randomNum = array[0];
        setStateAuthentication(randomNum);
    };

    const setAuthData = (newSub: string | null, newRole: string | null, newName: string | null) => {
        setSub(newSub);
        setRole(newRole);
        setName(newName);
    };

    const value: AuthContextType = {
        stateAuthentication,
        sub,
        role,
        name,
        generateRandomNumber,
        setAuthData,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
