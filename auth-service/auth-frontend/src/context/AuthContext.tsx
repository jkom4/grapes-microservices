import React, { createContext, useContext, useEffect, useRef, useState } from "react";
import {href, useLocation, useNavigate} from "react-router-dom";
import {checkSession, refresh} from "../services/authService";

// Helper function to parse JWT token and extract its payload
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
    setIsAuthenticated: (isAuthenticated: boolean) => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const intervalRef = useRef<NodeJS.Timeout | null>(null);
    const countdownRef = useRef<NodeJS.Timeout | null>(null);
    const REFRESH_INTERVAL_MINUTES = 10;
    const timeLeftRef = useRef<number>(60 * REFRESH_INTERVAL_MINUTES);

    const location = useLocation();
    const navigate = useNavigate();

    const [token, setTokenState] = useState<string | null>(localStorage.getItem("accessToken"));
    const [role, setRole] = useState<string | null>(() => parseJwt(token!)?.role || null);
    const [id, setId] = useState<string | null>(() => parseJwt(token!)?.sub || null);
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(!!token);

    // Update the token and related data (role, id, session)
    const setToken = async (newToken: string | null) => {
        if (newToken) {
            localStorage.setItem("accessToken", newToken);
        } else {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
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

    // Refresh the session (check token validity or refresh it)
    const refreshSession = async () => {
        const storedToken = localStorage.getItem("accessToken");
        const storedRefresh = localStorage.getItem("refreshToken");
        const protectedPaths = ["/dashboard", "/admin"];
        const onProtectedPage = protectedPaths.includes(location.pathname);

        if (storedRefresh) {
            try {
                const accessToken = await refresh(storedRefresh);
                localStorage.setItem('accessToken', accessToken);
            } catch (error) {
                console.error("Error refreshing token:", error);
            }

        }

        if (onProtectedPage && !storedToken) {
            if (storedRefresh) {
                return;
            }
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

    // Reset the refresh interval and start countdown logger
    const resetInterval = () => {
        if (intervalRef.current) clearInterval(intervalRef.current);
        if (countdownRef.current) clearInterval(countdownRef.current);

        timeLeftRef.current = 60 * REFRESH_INTERVAL_MINUTES;

        intervalRef.current = setInterval(async () => {
            await refreshSession();
            timeLeftRef.current = 60 * REFRESH_INTERVAL_MINUTES;
        }, REFRESH_INTERVAL_MINUTES * 60 * 1000);
    };

    useEffect(() => {
        if (isAuthenticated) {
            resetInterval();
        } else {
            if (intervalRef.current) clearInterval(intervalRef.current);
            if (countdownRef.current) clearInterval(countdownRef.current);
        }
        return () => {
            if (intervalRef.current) clearInterval(intervalRef.current);
            if (countdownRef.current) clearInterval(countdownRef.current);
        };
    }, [isAuthenticated]);

    (window as any).resetAuthRefreshInterval = resetInterval;

    return (
        <AuthContext.Provider value={{ isAuthenticated, token, role, id, setToken, setIsAuthenticated }}>
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
