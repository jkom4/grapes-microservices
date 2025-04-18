import { toast } from "react-toastify";
import { jwtDecode } from 'jwt-decode';
import {
    checkSession,
    disableUser,
    enableUser,
    getAllUsers,
    getUserById,
    getChallenge,
    logout,
    registerUser,
    updateUser,
    login, editPassword, editPin,
} from "../api/AuthApi";
import { AuthMethod, User } from "../models/User";

/**
 * Registers a new user.
 */
export const handleRegisterUser = async (user: User): Promise<void> => {
    const response = await registerUser(user);

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Registration failed");
    }
};

/**
 * Logs in the user and returns a message.
 */
export const handleGetChallenge = async (
    email: string,
    password: string,
    method: AuthMethod
): Promise<{ message: string }> => {
    try {
        const response = await getChallenge(email, password, method);
        const data = await response.json();
        return { message: data.message };
    } catch (error: any) {
        throw new Error(error.message || "Login failed");
    }
};

/**
 * Generates a SHA-256 digest from a challenge and a pin code.
 *
 * @param {string} challenge - The challenge string.
 * @param {string} pinCode - The user's pin code.
 * @returns {Promise<string>} The resulting SHA-256 hash as a hexadecimal string.
 */
const generateDigest = async (challenge: string, pinCode: string): Promise<string> => {
    const encoder = new TextEncoder();
    const data = encoder.encode(challenge + pinCode);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
};

/**
 * Verifies a challenge for 2FA or other challenge-based methods.
 */
export const handleLogin = async (
    challenge: string,
    pinCode: string,
    email: string,
    method: string
): Promise<string> => {
    console.log("test");
    const digest = await generateDigest(challenge, pinCode);
    const response = await login(digest, email, method);
    const data = await response.json();
    return data.token;
};

/**
 * Checks if the user session is still valid.
 */
export const checkSessionStatus = async (token: string): Promise<boolean> => {
    try {
        const response = await checkSession(token);
        return response.ok;
    } catch (error) {
        console.error("Error checking session:", error);
        return false;
    }
};

/**
 * Logs out the user and clears session data.
 */
export const handleLogout = async (
    setLoading: (loading: boolean) => void,
    setToken: (token: string | null) => void,
    navigate: (path: string) => void
) => {
    setLoading(true);
    const token = localStorage.getItem("jwt");

    if (!token) {
        setLoading(false);
        return;
    }

    try {
        const response = await logout(token);

        if (response.ok) {
            localStorage.removeItem("jwt");
            setToken(null);
            toast.success("Logged out successfully", { autoClose: 2000 });
            navigate("/");
        }
    } catch {
        toast.error("Logout failed.");
    } finally {
        setLoading(false);
    }
};

/**
 * Updates user data and optionally reloads the page.
 */
export const handleUserUpdate = async ({
                                           updatedUser,
                                           setUser,
                                           onSuccess,
                                           reload = false,
                                       }: {
    updatedUser: User;
    setUser?: (u: User) => void;
    onSuccess?: () => void;
    reload?: boolean;
}): Promise<Response | undefined> => {
    const token = localStorage.getItem("jwt");

    try {
        const response = await updateUser(updatedUser, token!);

        if (response.ok) {
            const updated = await response.json();
            setUser?.(updated);
            onSuccess?.();

            if (reload) {
                setTimeout(() => window.location.reload(), 2200);
            }

            return response;
        } else {
            const errorData = await response.json();

            const errorMessages =
                typeof errorData === "string"
                    ? errorData
                    : errorData.message && Object.keys(errorData).length === 1
                        ? errorData.message
                        : Object.entries(errorData)
                            .map(([field, message]) => `${field}: ${message}`)
                            .join("\n");

            toast.error(errorMessages, { autoClose: 2000 });

            return response;
        }
    } catch (err: any) {
        toast.error(err.message, { autoClose: 2000 });
        return undefined;
    }
};

/**
 * Disables a user account.
 */
export const handleDisableUser = async (
    id: string | undefined,
    token: string
): Promise<Response> => {
    const res = await disableUser(id, token);
    if (!res.ok) throw await res.json();
    return res;
};

/**
 * Enables a user account.
 */
export const handleEnableUser = async (
    id: string | undefined,
    token: string
): Promise<Response> => {
    const res = await enableUser(id, token);
    if (!res.ok) throw await res.json();
    return res;
};

/**
 * Fetches user by ID and optionally sets form/user state or handles password invalidity.
 */
export const handleFetchUserById = async (
    token: string,
    setUser?: (u: User) => void,
    setFormData?: (u: User) => void,
    onPasswordInvalid?: () => void
): Promise<void> => {
    const userId = JSON.parse(atob(token.split(".")[1])).sub;
    const fetchedUser = await getUserById(userId, token);

    if (!fetchedUser.passwordValid) {
        onPasswordInvalid?.();
    }

    setUser?.(fetchedUser);
    setFormData?.(fetchedUser);
};

/**
 * Loads all users and manages loading state.
 */
export const handleFetchAllUsers = async (
    token: string,
    setUsers: (users: User[]) => void,
    setLoading: (loading: boolean) => void
): Promise<void> => {
    try {
        setLoading(true);
        const users = await getAllUsers(token);
        setUsers(users);
    } catch (error: any) {
        throw new Error("Failed to load users: " + error.message);
    } finally {
        setLoading(false);
    }
};

/**
 * Updates a user's password.
 */
export const handleEditPassword = async (
    currentPassword: string,
    updatedPassword: string,
    token: string
): Promise<Response> => {
    try {
        const decoded = jwtDecode(token);
        const userId = decoded?.sub;

        if (!userId) {
            throw new Error("User ID not found in token");
        }

        const response = await editPassword(userId, currentPassword, updatedPassword, token);

        if (!response.ok) {
            const data = await response.json();
            throw new Error(data.message || "Failed to update password");
        }

        return response;
    } catch (error: any) {
        throw new Error(error || "An error occurred during the password update");
    }
};

/**
 * Updates a user's pin code.
 */
export const handleEditPin = async (
    currentPin: string,
    updatedPin: string,
    token: string
): Promise<Response> => {
    try {
        const decoded = jwtDecode(token);
        const userId = decoded?.sub;

        if (!userId) {
            throw new Error("User ID not found in token");
        }

        const response = await editPin(userId, currentPin, updatedPin, token);

        if (!response.ok) {
            const data = await response.json();
            throw new Error(data.message || "Failed to update pin");
        }

        return response;
    } catch (error: any) {
        throw new Error(error || "An error occurred during the pin update");
    }
};
