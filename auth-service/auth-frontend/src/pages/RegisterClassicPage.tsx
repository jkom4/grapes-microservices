import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {toast} from "react-toastify";
import {useAuth} from "../context/AuthContext";
import AlreadyAuthenticated from "../components/AlreadyAuthenticated";
import RegistrationForm from "../sections/RegistrationClassicForm";
import {registerUser} from "../services/authService";
import {Role} from "../models/User";

const RegisterClassicPage = () => {
    const navigate = useNavigate();
    const {isAuthenticated} = useAuth();
    const [loading, setLoading] = useState(false);
    const [useSameAddress, setUseSameAddress] = useState(true);
    const [errors] = useState<{ [key: string]: string }>({});
    const [error, setError] = useState<string | null>(null);
    const [formData, setFormData] = useState({
        name: '',
        firstName: '',
        password: '',
        email: '',
        emailVerified: false,
        phoneNumber: '',
        phoneVerified: false,
        nationalId: '',
        birthDate: '',
        age: 0,
        gender: 'MALE',
        pinCode: '',
        role: 'USER',
        profession: '',
        deliveryAddress: {
            street: '',
            number: '',
            postalCode: '',
            city: '',
            country: '',
        },
        billingAddress: {
            street: '',
            number: '',
            postalCode: '',
            city: '',
            country: '',
        },
        active: true,
        passwordValid: true,
        authMeans: {
            EMAIL: {
                enabled: true,
                lastLogin: new Date().toISOString(),
                counter: 0,
            },
            SMS: {
                enabled: true,
                lastLogin: new Date().toISOString(),
                counter: 0,
            },
            EID: {
                enabled: true,
                lastLogin: new Date().toISOString(),
                counter: 0,
            }
        },
    });

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            navigate('/dashboard');
        }
    }, [navigate]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleAddressChange = (e: React.ChangeEvent<HTMLInputElement>, addressType: 'deliveryAddress' | 'billingAddress') => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [addressType]: {
                ...prev[addressType],
                [name]: value
            }
        }));
    };

    if (isAuthenticated) {
        return <AlreadyAuthenticated />;
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);

        const validGender: 'MALE' | 'FEMALE' | 'OTHER' =
            ['MALE', 'FEMALE', 'OTHER'].includes(formData.gender as any)
                ? (formData.gender as 'MALE' | 'FEMALE' | 'OTHER')
                : 'OTHER';

        const createAuthMeans = () => ({
            enabled: true,
            lastLogin: new Date().toISOString(),
            counter: 0,
        });

        const dataToSend = {
            ...formData,
            gender: validGender,
            role: 'USER' as Role,
            billingAddress: useSameAddress ? formData.deliveryAddress : formData.billingAddress,
            emailVerified: false,
            phoneVerified: false,
            authMeans: {
                EMAIL: createAuthMeans(),
                SMS: createAuthMeans(),
                EID: createAuthMeans(),
            },
            active: true,
            createdAt: '',
            updatedAt: '',
        };

        try {
            await registerUser(dataToSend);
            setFormData(initialFormData);
            toast.success('Registration successful', { autoClose: 2000 });
            navigate('/');
        } catch (err: any) {
            handleErrors(err);
        } finally {
            setLoading(false);
        }
    };

    const handleErrors = (err: any) => {
        if (typeof err === 'object' && err !== null) {
            const formattedErrors = Object.entries(err)
                .map(([key, message]) => {
                    if (typeof message === 'string') {
                        toast.error(message, { autoClose: 2000 });
                    } else {
                        toast.error("An unknown error occurred", { autoClose: 2000 });
                    }
                    return '';
                })
                .join('\n');
            if (formattedErrors) {
                setError(formattedErrors);
            }
        } else {
            setError("An unexpected error has occurred. Please verify the form");
        }
    };

    const initialFormData = {
        name: '',
        firstName: '',
        password: '',
        passwordValid: false,
        email: '',
        emailVerified: false,
        phoneNumber: '',
        phoneVerified: false,
        nationalId: '',
        birthDate: '',
        age: 0,
        gender: 'MALE',
        pinCode: '',
        role: 'USER',
        profession: '',
        deliveryAddress: { street: '', postalCode: '', country: '', number: '', city: '' },
        billingAddress: { street: '', postalCode: '', country: '', number: '', city: '' },
        active: true,
        authMeans: {
            EMAIL: { enabled: true, lastLogin: new Date().toISOString(), counter: 0},
            SMS: { enabled: true, lastLogin: new Date().toISOString(), counter: 0},
            EID: { enabled: true, lastLogin: new Date().toISOString(), counter: 0}
        },
        createdAt: '',
        updatedAt: '',
    };

    return (
        <RegistrationForm
            formData={formData}
            setFormData={setFormData}
            handleChange={handleChange}
            handleAddressChange={handleAddressChange}
            handleSubmit={handleSubmit}
            errors={errors}
            loading={loading}
            error={error}
            useSameAddress={useSameAddress}
            setUseSameAddress={setUseSameAddress}
        />    );
};

export default RegisterClassicPage;
