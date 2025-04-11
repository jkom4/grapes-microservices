// src/api/authApi.ts

import { User, AuthMethod } from '../models/User';

const API_BASE_URL = 'http://localhost:8091';

const headers = {
    'Content-Type': 'application/json',
};

// /auth/login
export async function login(email: string, password: string, method: AuthMethod): Promise<string> {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ email, password, method }),
    });
    return res.text(); // Juste un string
}

// /auth/verify-challenge
export async function verifyChallenge(submittedChallenge: number, email: string): Promise<string> {
    const res = await fetch(`${API_BASE_URL}/auth/verify-challenge`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ submittedChallenge, email }),
    });
    return res.text(); // Token JWT
}

// /auth/logout
export async function logout(token: string): Promise<string> {
    const res = await fetch(`${API_BASE_URL}/auth/logout`, {
        method: 'POST',
        headers: {
            ...headers,
            Authorization: `Bearer ${token}`,
        },
    });
    return res.text();
}

// /auth/session
export async function checkSession(token: string): Promise<boolean> {
    const res = await fetch(`${API_BASE_URL}/auth/session`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
    return res.json(); // boolean
}

// /users/register
export async function registerUser(user: User): Promise<User> {
    const res = await fetch(`${API_BASE_URL}/users/register`, {
        method: 'POST',
        headers,
        body: JSON.stringify(user),
    });
    return res.json();
}

// /users/{id}
export async function getUserById(id: string, token: string): Promise<User> {
    const res = await fetch(`${API_BASE_URL}/users/${id}`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
    return res.json();
}

// /users/update/password
export async function updatePassword(password: string, token: string): Promise<string> {
    const res = await fetch(`${API_BASE_URL}/users/update/password`, {
        method: 'POST',
        headers: {
            ...headers,
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ password }),
    });
    return res.text();
}

// /users/update
export async function updateUser(user: User, token: string): Promise<User> {
    const res = await fetch(`${API_BASE_URL}/users/update`, {
        method: 'PUT',
        headers: {
            ...headers,
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(user),
    });
    return res.json();
}
