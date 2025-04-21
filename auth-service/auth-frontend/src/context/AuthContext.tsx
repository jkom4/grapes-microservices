import React, { createContext, useContext, useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { checkSession } from "../services/authService";

/**
 * Decodes a JWT token and returns its payload.
 */
const parseJwt = (token: string) => {
    try {
        const base64Url = token.split(".")[1];
        const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split("")
                .map((c) => `%${("00" + c.charCodeAt(0).toString(16)).slice(-2)}`)
                .join("")
        );
        return JSON.parse(jsonPayload);
    } catch {
        return null;
    }
};

type AuthContextType = {
    isAuthenticated: boolean;
    token: string | null;
    role: string | null;
    id: string | null;
    setToken: (token: string | null) => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const location = useLocation();
    const navigate = useNavigate();

    const [token, setTokenState] = useState<string | null>(
        localStorage.getItem("jwt")
    );
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(!!token);
    const [role, setRole] = useState<string | null>(() => {
        const decoded = token ? parseJwt(token) : null;
        return decoded?.role || null;
    });
    const [id, setId] = useState<string | null>(() => {
        const decoded = token ? parseJwt(token) : null;
        return decoded?.sub || null;
    });

    const setToken = async (newToken: string | null) => {
        if (newToken) {
            localStorage.setItem("jwt", newToken);
        } else {
            localStorage.removeItem("jwt");
        }

        setTokenState(newToken);

        if (newToken) {
            const decoded = parseJwt(newToken);
            setRole(decoded?.role || null);
            setId(decoded?.sub || null);
            try {
                const valid = await checkSession(newToken);
                setIsAuthenticated(valid.ok);
            } catch {
                setIsAuthenticated(false);
            }
        } else {
            setIsAuthenticated(false);
            setRole(null);
            setId(null);
        }
    };

    useEffect(() => {
        const refreshSession = async () => {
            const storedToken = localStorage.getItem("jwt");
            const onDashboard = location.pathname === "/dashboard";

            if (onDashboard && !storedToken) {
                setIsAuthenticated(false);
                navigate("/");
                return;
            }

            if (storedToken) {
                try {
                    const valid = await checkSession(storedToken);
                    if (!valid.ok) {
                        await setToken(null);
                        navigate("/");
                    }
                } catch {
                    await setToken(null);
                    navigate("/");
                }
            }
        };

        const interval = setInterval(refreshSession, 5 * 1000);
        return () => clearInterval(interval);
    }, [navigate, location]);

    return (
        <AuthContext.Provider
            value={{ isAuthenticated, token, role, id, setToken }}
        >
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within AuthProvider");
    }
    return context;
};
