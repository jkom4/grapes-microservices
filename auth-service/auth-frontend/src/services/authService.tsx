import {AuthMethod, User} from '../models/User';
import {jwtDecode} from 'jwt-decode';

const API_BASE_URL = 'http://89.168.47.217:8090/api';

const headers = {
    'Content-Type': 'application/json',
};

function handleConnectionError(error: any) {
    if (error instanceof TypeError && error.message.includes('Failed to fetch')) {
        throw new Error('Connection failed. Please check your network or the API server.');
    }
    throw error;
}

export async function getChallenge(email: string, password: string, method: AuthMethod): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/auth/challenge`, {
            method: 'POST',
            headers,
            body: JSON.stringify({email, password, authMethod: method}),
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }
        return res;
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Get challenge] Error:', error);
        throw error;
    }
}

export async function login(challenge: string, pin: string, email: string, method: string): Promise<string> {
    try {
        const digest = await computeDigest(challenge, pin);
        const res = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers,
            body: JSON.stringify({email, digest, authMethod: method}),
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }
        const json = await res.json();
        return json.accessToken;
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Login] Error:', error);
        throw error;
    }
}

async function computeDigest(challenge: string, pin: string): Promise<string> {
    const combined = challenge + pin;
    const encoder = new TextEncoder();
    const data = encoder.encode(combined);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

export async function logout(token: string): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/auth/logout`, {
            method: 'POST',
            headers: {
                ...headers,
                Authorization: `Bearer ${token}`,
            },
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }
        return res;
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Logout] Error:', error);
        throw error;
    }
}

export async function refresh(refreshToken: string): Promise<string> {
    try {
        const res = await fetch(`${API_BASE_URL}/auth/refresh`, {
            method: 'POST',
            headers: {
                ...headers,
            },
            body: JSON.stringify({refreshToken}),
        });

        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }

        const data = await res.json();
        return data.accessToken;
    } catch (error: any) {
        console.error('[Refresh] Error:', error);
        throw error;
    }
}

export async function getRefreshToken(token: string): Promise<string> {
    try {
        const res = await fetch(`${API_BASE_URL}/auth/get-refresh`, {
            method: 'POST',
            headers: {
                ...headers,
                Authorization: `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }

        const json = await res.json();
        return json.refreshToken;
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Get Refresh Token] Error:', error);
        throw error;
    }

}

export async function checkSession(token: string): Promise<{ ok: boolean }> {
    try {
        const res = await fetch(`${API_BASE_URL}/auth/session`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }

        return {ok: true};
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Check Session] Error:', error);

        return {ok: false};
    }
}

export async function registerUser(user: User): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/users/register`, {
            method: 'POST',
            headers,
            body: JSON.stringify(user),
        });
        if (!res.ok) {
            throw await res.json();
        }
        return res;
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Register] Error:', error);
        throw error;
    }
}

export async function registerWithEid(email: string, password: string, phoneNumber: string): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/users/register/eid`, {
            method: 'POST',
            headers,
            body: JSON.stringify({email, password, phoneNumber}),
        });
        if (!res.ok) {
            throw await res.json();
        }
        return res;
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Register with EID] Error:', error);
        throw error;
    }
}


export async function getUserById(id: string, token: string): Promise<User> {
    try {
        const res = await fetch(`${API_BASE_URL}/users/${id}`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }
        return await res.json();
    } catch (error: any) {
        handleConnectionError(error);
        throw error;
    }
}

export async function editPassword(currentPassword: string, updatedPassword: string, token: string): Promise<Response> {
    try {
        const decoded = jwtDecode(token);
        const id = decoded?.sub;

        const res = await fetch(`${API_BASE_URL}/users/${id}/password`, {
            method: 'PUT',
            headers: {
                ...headers,
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({currentPassword, updatedPassword}),
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }
        return res;
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Update Password] Error:', error);
        throw error;
    }
}

export async function editPin(currentPin: string, updatedPin: string, token: string): Promise<Response> {
    try {
        const decoded = jwtDecode(token);
        const id = decoded?.sub;

        const res = await fetch(`${API_BASE_URL}/users/${id}/pin`, {
            method: 'PUT',
            headers: {
                ...headers,
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({currentPin, updatedPin}),
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }
        return res;
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Update Pin] Error:', error);
        throw error;
    }
}

export async function updateUser(user: User, token: string): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/users/${user.id}`, {
            method: 'PUT',
            headers: {
                ...headers,
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(user),
        });
        if (!res.ok) {
            const errorJson = await res.json().catch(() => ({}));
            const firstError = Object.values(errorJson)?.[0] ?? 'Unknown error';
            throw new Error(firstError as string);
        }
        return res;
    } catch (error: any) {
        console.error('[Update User] Error:', error);
        throw error;
    }
}

export async function disableUser(id: string | undefined, token: string): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/users/disable/${id}`, {
            method: 'PUT',
            headers: {
                ...headers,
                Authorization: `Bearer ${token}`,
            },
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }
        return res;
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Disable User] Error:', error);
        throw error;
    }
}

export async function enableUser(id: string | undefined, token: string): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/users/enable/${id}`, {
            method: 'PUT',
            headers: {
                ...headers,
                Authorization: `Bearer ${token}`,
            },
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }
        return res;
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Enable User] Error:', error);
        throw error;
    }
}

export async function getAllUsers(token: string): Promise<User[]> {
    try {
        const res = await fetch(`${API_BASE_URL}/users`, {
            method: 'GET',
            headers: {
                ...headers,
                Authorization: `Bearer ${token}`,
            },
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(errorText.message);
        }
        return await res.json();
    } catch (error: any) {
        handleConnectionError(error);
        console.error('[Get All Users] Error:', error);
        throw error;
    }
}

export function returnTokenToTierceApp(accessToken: string, navigate: (path: string) => void): void {
    const rawRedirectUrl = sessionStorage.getItem('redirect_uri');
    const state = sessionStorage.getItem('state');

    if (!rawRedirectUrl) {
        navigate('/');
        return;
    }

    try {
        const url = new URL(rawRedirectUrl);
        if (state) {
            url.searchParams.set('state', state);
        }
        url.searchParams.set('accessToken', accessToken);

        //clean up session storage
        sessionStorage.removeItem('redirect_uri');
        sessionStorage.removeItem('state');
        sessionStorage.removeItem('client_id');

        window.location.href = url.toString();
    } catch (error) {
        console.error('Invalid redirect URL:', error);
        navigate('/');
    }
}
