// src/models/User.ts

export type AuthMethod =
    | 'EMAIL'
    | 'SMS'
    | 'EID'

export interface AuthMean {
    enabled: boolean;
    lastLogin: string;
    counter: number;
}

export interface Address {
    street: string;
    postalCode: string;
    country: string;
    number: string;
    city: string;
}

export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export type Role = 'USER' | 'ADMIN' | 'DELIVERY';

export interface User {
    id?: string;
    active?: boolean;
    passwordValid: boolean;
    name: string;
    firstName: string;
    email: string;
    emailVerified: boolean;
    password: string;
    phoneNumber: string;
    phoneVerified: boolean;
    nationalId: string;
    age: number;
    birthDate: string; // ISO format, ex: '1990-01-01'
    gender: Gender;
    pinCode: string;
    loyaltyPoints?: number;
    role: Role;
    profession: string;
    authMeans: Record<AuthMethod, AuthMean>;
    deliveryAddress: Address;
    billingAddress: Address;
    createdAt: string; // ISO format, ex: '2023-10-01T12:00:00Z'
    updatedAt: string; // ISO format, ex: '2023-10-01T12:00:00Z'
}
