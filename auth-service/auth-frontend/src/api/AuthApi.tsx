import {AuthMethod, User} from '../models/User';

const API_BASE_URL = 'http://localhost:8090/api';

const headers = {
    'Content-Type': 'application/json',
};

/**
 * Handles connection errors by checking if the error is a TypeError with a specific message.
 */
function handleConnectionError(error: any) {
    if (error instanceof TypeError && error.message.includes('Failed to fetch')) {
        throw 'Connection failed. Please check your network or the API server.';
    }
    throw error;
}

/**
 * Login a user with the provided credentials and authentication method.
 * @param email - The user's email address.
 * @param password - The user's password.
 * @param method - The authentication method used (e.g., eID, OTP).
 * @returns A Promise with the server response.
 */
export async function getChallenge(email: string, password: string, method: AuthMethod): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/auth/challenge`, {
            method: 'POST',
            headers,
            body: JSON.stringify({email, password, authMethod: method}),
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw new Error(`${errorText.message}`);
        }

        return res;
    } catch (error) {
        handleConnectionError(error);
        console.error('[Login] Error:', error);
        throw error;
    }
}

/**
 * Verifies the submitted challenge code for multifactorial authentication.
 * @param digest - The challenge code entered by the user.
 * @param email - The user's email address.
 * @param method - The authentication method used.
 * @returns A Promise with the server response.
 */
export async function login(digest: string, email: string, method: string): Promise<Response> {
    try {

        const res = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers,
            body: JSON.stringify({email, digest, authMethod: method}),
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw `${errorText.message}`;
        }
        return res;
    } catch (error) {
        handleConnectionError(error);
        console.error('[Login] Error:', error);
        throw error;
    }
}

/**
 * Logs the user out of the system by invalidating their session.
 * @param token - The user's authentication token.
 * @returns A Promise with the server response.
 */
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
            throw `${errorText.message}`;
        }
        return res;
    } catch (error) {
        handleConnectionError(error);
        console.error('[Logout] Error:', error);
        throw error;
    }
}

/**
 * Checks the current session's validity by verifying the provided token.
 * @param token - The user's authentication token.
 * @returns A Promise with the server response.
 */
export async function checkSession(token: string): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/auth/session`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw `${errorText.message}`;
        }
        return res;
    } catch (error) {
        handleConnectionError(error);
        console.error('[Check Session] Error:', error);
        throw error;
    }
}

/**
 * Registers a new user with the provided user data.
 * @param user - The user object containing registration details.
 * @returns A Promise with the server response.
 */
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
    } catch (error) {
        handleConnectionError(error);
        console.error('[Register] Error:', error);
        throw error;
    }
}

/**
 * Fetches a user by their ID.
 * @param id - The user's unique identifier.
 * @param token - The authentication token for authorization.
 * @returns A Promise resolving to the user object.
 */
export async function getUserById(id: string, token: string): Promise<User> {
    try {
        const res = await fetch(`${API_BASE_URL}/users/${id}`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw `${errorText.message}`;
        }
        return await res.json();
    } catch (error) {
        handleConnectionError(error);
        throw error;
    }
}

/**
 * Updates the user's password.
 * @param id
 * @param currentPassword - The current password of the user.
 * @param updatedPassword - The new password to be set.
 * @param token - The authentication token for authorization.
 * @returns A Promise with the server response.
 */
export async function editPassword(id: String, currentPassword: string, updatedPassword: string, token: string): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/users/${id}/password`, {
            method: 'PUT',
            headers: {
                ...headers,
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
                currentPassword,
                updatedPassword,
            }),
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw `${errorText.message}`;
        }
        return res;
    } catch (error) {
        handleConnectionError(error);
        console.error('[Update Password] Error:', error);
        throw error;
    }
}

/**
 * Updates the user's password.
 * @param id - The user's unique identifier.
 * @param currentPin - The current pin of the user.
 * @param updatedPin - The new pin to be set.
 * @param token - The authentication token for authorization.
 * @returns A Promise with the server response.
 */
export async function editPin(id: string, currentPin: string, updatedPin: string, token: string): Promise<Response> {
    try {
        const res = await fetch(`${API_BASE_URL}/users/${id}/pin`, {
            method: 'PUT',
            headers: {
                ...headers,
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
                currentPin,
                updatedPin,
            }),
        });
        if (!res.ok) {
            const errorText = await res.json();
            throw `${errorText.message}`;
        }
        return res;
    } catch (error) {
        handleConnectionError(error);
        console.error('[Update Pin] Error:', error);
        throw error;
    }
}

/**
 * Updates the user's profile information.
 * @param user - The updated user object.
 * @param token - The authentication token for authorization.
 * @returns A Promise with the server response.
 */
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
            const errorText = await res.json();
            throw `${errorText.message}`;
        }
        return res;
    } catch (error) {
        handleConnectionError(error);
        console.error('[Update User] Error:', error);
        throw error;
    }
}

/**
 * Disables a user's account.
 * @param id - The user's unique identifier.
 * @param token - The authentication token for authorization.
 * @returns A Promise with the server response.
 */
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
            throw `${errorText.message}`;
        }
        return res;
    } catch (error) {
        handleConnectionError(error);
        console.error('[Disable User] Error:', error);
        throw error;
    }
}

/**
 * Enables a user's account.
 * @param id - The user's unique identifier.
 * @param token - The authentication token for authorization.
 * @returns A Promise with the server response.
 */
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
            throw `${errorText.message}`;
        }

        return res;
    } catch (error) {
        handleConnectionError(error);
        console.error('[Enable User] Error:', error);
        throw error;
    }
}

/**
 * Fetches all users.
 * @param token - The authentication token for authorization.
 * @returns A Promise resolving to a list of users.
 */
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
            throw `${errorText.message}`;
        }

        return await res.json();
    } catch (error) {
        handleConnectionError(error);
        console.error('[Get All Users] Error:', error);
        throw error;
    }
}
