// src/models/User.ts

export type AuthMethod =
    | 'EMAIL'
    | 'SMS'

export interface AuthMean {
    enabled: boolean;
    name: AuthMethod;
    publicKey: string | null;
    challenge: string | null;
    lastLogin: string; // Date ISO format
    counter: number | null;
    token: string | null;
}

export interface Address {
    street: string;
    postalCode: string;
    country: string;
    countryCode: string;
    number: string;
    province: string;
}

export type Gender = 'Male' | 'Female' | 'Other';

export type Role = 'USER' | 'ADMIN' | 'DELIVERY';

export interface User {
    id: string;
    isActive: boolean;
    bankId: string;
    name: string;
    firstName: string;
    email: string;
    emailVerified: boolean;
    phoneNumber: string;
    phoneVerified: boolean;
    nationalId: string;
    birthDate: string; // ISO format, ex: '1990-01-01'
    age: number;
    gender: Gender;
    cardNumber: string;
    pinCode: string;
    loyaltyPoints: number;
    role: Role;
    profession: string;
    authMethods: Record<string, AuthMean>;
    deliveryAddress: Address;
    billingAddress: Address;
}
