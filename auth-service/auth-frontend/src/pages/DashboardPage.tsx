import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {User} from "../models/User";
import { useAuth } from "../context/AuthContext";
import { toast } from "react-toastify";
import { handleDisableUser, handleFetchUserById, handleUserUpdate } from "../services/authService";
import NotAuthenticated from "../components/NotAuthenticated";
import Dashboard from "../sections/Dashboard";

/**
 * DashboardPage component to display and manage user profile data.
 */
const DashboardPage = () => {
    const { isAuthenticated, token, setToken } = useAuth();
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    // State variables for managing user details, form data, and modal visibility
    const [useSameAddress, setUseSameAddress] = useState(true);
    const [showModalPassword, setShowModalPassword] = useState(false);
    const [showModalPin, setShowModalPin] = useState(false);
    const [user, setUser] = useState<User | null>(null);
    const [isFormUnchanged, setIsFormUnchanged] = useState(true);
    const [formData, setFormData] = useState<User>({
        id: '',
        name: '',
        firstName: '',
        passwordValid: true,
        email: '',
        emailVerified: false,
        phoneNumber: '',
        phoneVerified: false,
        nationalId: '',
        birthDate: '',
        age: 0,
        gender: 'MALE',
        role: 'USER',
        profession: '',
        deliveryAddress: {
            street: '',
            postalCode: '',
            country: '',
            number: '',
            city: '',
        },
        billingAddress: {
            street: '',
            postalCode: '',
            country: '',
            number: '',
            city: '',
        },
        active: true,
        authMeans: {
            EMAIL: { enabled: true, lastLogin: new Date().toISOString(), counter: 0 },
            SMS: { enabled: true, lastLogin: new Date().toISOString(), counter: 0 },
            EID: { enabled: true, lastLogin: new Date().toISOString(), counter: 0 }
        },
        createdAt: '',
        updatedAt: '',
    });

    // Verify user session and fetch data on component mount
    useEffect(() => {
        const verifySession = async () => {
            if (isAuthenticated && token) {
                await handleFetchUserById(token, setUser, setFormData, () => setShowModalPassword(true));
            }
        };
        verifySession();
    }, [token, isAuthenticated]);

    useEffect(() => {
        if (user) {
            setIsFormUnchanged(JSON.stringify(formData) === JSON.stringify(user));
        }
    }, [formData, user]);

    // If the user is not authenticated, show the NotAuthenticated component
    if (!isAuthenticated) {
        return <NotAuthenticated />;
    }

    /**
     * Handles input field changes and updates the form data.
     * @param e - The change event of the input field.
     */
    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    /**
     * Handles address-specific changes (delivery or billing address).
     * @param e - The change event of the input field.
     */
    const handleAddressChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        const [section, field] = name.split('.'); // e.g., 'deliveryAddress.street' → ['deliveryAddress', 'street']

        setFormData(prevData => ({
            ...prevData,
            [section]: { ...prevData[section as 'deliveryAddress' | 'billingAddress'], [field]: value }
        }));
    };

    /**
     * Handles form submission, updates user information if changes are made.
     * @param e - The form submit event.
     */
    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setLoading(true);

        if (JSON.stringify(formData) === JSON.stringify(user)) {
            toast.error('Nothing was changed', { autoClose: 2000 });
            setLoading(false);
            return;
        }

        try {
            const res = await handleUserUpdate({
                updatedUser: { ...formData },
                setUser,
                onSuccess: () => toast.success('User updated successfully!', { autoClose: 2000 }),
            });
            if (!res?.ok) {
                toast.error('User updated failed!', { autoClose: 2000 });
            }
        } catch (err: any) {
            const temp : string = "Error while updating user : `{err.message}`";
        }
        setLoading(false);
    };

    /**
     * Handles account disable action.
     */
    const handleDisableAccount = async () => {
        setLoading(true);
        const token = localStorage.getItem('jwt');
        if (!token || !user) return;

        try {
            const response = await handleDisableUser(user.id, token);
            toast.success('Your account has been disabled.', { autoClose: 2000 });
            if (response.ok) {
                setToken(null);
                localStorage.removeItem('jwt');
                navigate('/');
            } else {
                const errorData = await response.json();
                const errorMessages = Object.entries(errorData)
                    .map(([field, message]) => `${field}: ${message}`)
                    .join('\n');
                toast.error(errorMessages, { autoClose: 2000 });
            }
        } catch (err: unknown) {
            if (err instanceof Error) {
                toast.error(err.message, { autoClose: 2000 });
            } else {
                toast.error('An unknown error occurred', { autoClose: 2000 });
            }
        } finally {
            setLoading(false);
        }
    };

    /**
     * Triggered when the password change modal is to be shown.
     */
    const handlePasswordChange = () => {
        setShowModalPassword(true);
    };

    const handlePinChange = () => {
        setShowModalPin(true);
    }

    /**
     * Closes the password change modal.
     */
    const handleCloseModalPassword = () => {
        setShowModalPassword(false);
    };

    const handleCloseModalPin = () => {
        setShowModalPin(false);
    };

    // If no token is found, do not render the page
    if (!token) return null;

    return (
        <Dashboard
            formData={formData}
            setFormData={setFormData}
            useSameAddress={useSameAddress}
            setUseSameAddress={setUseSameAddress}
            loading={loading}
            showModalPassword={showModalPassword}
            showModalPin={showModalPin}
            token={token}
            handleChange={handleChange}
            handleAddressChange={handleAddressChange}
            handleSubmit={handleSubmit}
            handlePasswordChange={handlePasswordChange}
            handlePinChange={handlePinChange}
            handleCloseModalPassword={handleCloseModalPassword}
            handleCloseModalPin={handleCloseModalPin}
            handleDisableAccount={handleDisableAccount}
            isFormUnchanged={isFormUnchanged}
        />
    );
};

export default DashboardPage;
