import React from 'react';
import Loader from '../components/Loader';
import {User} from "../models/User";
import {editPassword, editPin} from "../services/authService";
import UpdateCredentialModal from "../modals/UpdateCredentialModal";

interface Address {
    street: string;
    number: string;
    postalCode: string;
    city: string;
    country: string;
}

interface FormData {
    firstName: string;
    name: string;
    email: string;
    phoneNumber: string;
    gender: string;
    profession: string;
    deliveryAddress: Address;
    billingAddress: Address;
    authMeans: {
        [key: string]: {
            enabled: boolean;
            lastLogin: string;
            counter: number;
        };
    };
    createdAt: string;
    updatedAt: string;
}

interface DashboardProps {
    loading: boolean;
    showModalPassword: boolean;
    showModalPin: boolean;
    token: string;
    formData: FormData;
    setFormData: React.Dispatch<React.SetStateAction<User>>;
    useSameAddress: boolean;
    handleCloseModalPassword: () => void;
    handleCloseModalPin: () => void;
    handleSubmit: (e: React.FormEvent<HTMLFormElement>) => Promise<void>;
    handleChange: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void;
    handleAddressChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    setUseSameAddress: (value: boolean) => void;
    handlePasswordChange: () => void;
    handlePinChange: () => void;
    handleDisableAccount: () => void;
    isFormUnchanged: boolean;
}

function formatDate(dateString: string): string {
    if (!dateString) return '/';
    const date = new Date(dateString);
    const options: Intl.DateTimeFormatOptions = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: 'numeric',
        minute: 'numeric',
        hour12: false,
    };
    return date.toLocaleString('en-EN', options);
}

const Dashboard: React.FC<DashboardProps> = ({
                                                 loading,
                                                 showModalPassword,
                                                 showModalPin,
                                                 token,
                                                 formData,
                                                 useSameAddress,
                                                 handleCloseModalPassword,
                                                 handleCloseModalPin,
                                                 handleSubmit,
                                                 handleChange,
                                                 handleAddressChange,
                                                 setUseSameAddress,
                                                 handlePasswordChange,
                                                 handlePinChange,
                                                 handleDisableAccount,
                                                 isFormUnchanged
                                             }) => {
    return (
        <div>
            {loading && <Loader/>}
            <UpdateCredentialModal
                showModal={showModalPassword}
                token={token}
                onClose={() => (handleCloseModalPassword())}
                credentialType="password"
                handleUpdate={(current: any, updated: string, token: string) =>
                    editPassword(current, updated, token)
                }
            />
            <UpdateCredentialModal
                showModal={showModalPin}
                token={token}
                onClose={() => handleCloseModalPin()}
                credentialType="pinCode"
                handleUpdate={(current, updated) =>
                    editPin(current, updated, token)
                }
            />
            <div className="min-h-screen bg-gray-50 px-4 py-8">
                <h1 className="text-4xl font-bold text-center text-gray-800 mb-10">👤 User Dashboard</h1>

                <form onSubmit={(e) => {
                    e.preventDefault();
                    handleSubmit(e as React.FormEvent<HTMLFormElement>);
                }}
                      className="space-y-12 max-w-4xl mx-auto bg-white p-8 rounded-xl shadow-xl">
                    <section className="space-y-6">
                        <h2 className="text-2xl font-semibold text-gray-700">📄 Account Information</h2>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                            <InputField id="firstName" label="First Name" value={formData.firstName}
                                        onChange={handleChange}/>
                            <InputField id="name" label="Last Name" value={formData.name} onChange={handleChange}/>
                            <InputField id="email" label="Email" type="email" value={formData.email}
                                        onChange={handleChange}/>
                            <InputField id="phoneNumber" label="Phone Number" type="tel" value={formData.phoneNumber}
                                        onChange={handleChange}/>
                            <div className="space-y-2">
                                <label htmlFor="gender" className="text-sm font-medium text-gray-700">Gender</label>
                                <select
                                    id="gender"
                                    name="gender"
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                    value={formData.gender}
                                    onChange={handleChange}
                                >
                                    <option value="MALE">Male</option>
                                    <option value="FEMALE">Female</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                            <InputField id="profession" label="Profession" value={formData.profession}
                                        onChange={handleChange}/>
                        </div>
                        <div className="mt-4 flex flex-row gap-6">
                            <button
                                type="button"
                                onClick={handlePasswordChange}
                                className="text-sm font-medium text-blue-600 hover:underline"
                            >
                                🔒 Change Password
                            </button>
                            <button
                                type="button"
                                onClick={handlePinChange}
                                className="text-sm font-medium text-blue-600 hover:underline"
                            >
                                🔒 Change PIN Code
                            </button>
                        </div>
                    </section>

                    {/* Add CreatedAt and UpdatedAt sections */}
                    <section className="space-y-6 mt-8">
                        <h2 className="text-2xl font-semibold text-gray-700">📅 Account Dates</h2>
                        <div className="space-y-2">
                            <p className="text-sm text-gray-600">Created At: {formatDate(formData.createdAt)}</p>
                            <p className="text-sm text-gray-600">Updated At: {formatDate(formData.updatedAt)}</p>
                        </div>
                    </section>

                    <section className="space-y-6">
                        <h2 className="text-2xl font-semibold text-gray-700">📦 Address Information</h2>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                            <AddressSection
                                title="Delivery Address"
                                address={formData.deliveryAddress}
                                prefix="deliveryAddress"
                                handleChange={handleAddressChange}
                            />

                            {!useSameAddress && (
                                <AddressSection
                                    title="Billing Address"
                                    address={formData.billingAddress}
                                    prefix="billingAddress"
                                    handleChange={handleAddressChange}
                                />
                            )}
                        </div>
                        <div className="flex items-center gap-2">
                            <input type="checkbox" id="sameAddress" checked={useSameAddress}
                                   onChange={() => setUseSameAddress(!useSameAddress)}/>
                            <label htmlFor="sameAddress" className="text-sm text-gray-600">Billing address is the same
                                as delivery</label>
                        </div>
                    </section>

                    <section className="space-y-6">
                        <h2 className="text-2xl font-semibold text-gray-700">🔐 Authentication Methods</h2>
                        <div className="space-y-4">
                            {Object.keys(formData.authMeans).map((authMethod) => {
                                const auth = formData.authMeans[authMethod as keyof typeof formData.authMeans];
                                return (
                                    <div key={authMethod}
                                         className="flex justify-between items-center border-b border-gray-300 pb-4">
                                        <div>
                                            <h3 className="text-xl font-semibold text-gray-800 capitalize">{authMethod}</h3>
                                            <p className="text-sm text-gray-600">Last
                                                login: {formatDate(auth.lastLogin)}</p>
                                        </div>
                                        <div className="flex items-center space-x-4">
                        <span
                            className={`px-3 py-1 text-sm rounded-full ${
                                auth.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                            }`}
                        >
                            {auth.enabled ? 'Enabled' : 'Disabled'}
                        </span>
                                            <p className="text-sm text-gray-600">Attempts: {auth.counter}</p>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </section>
                    <div className="flex justify-between items-center mt-8">
                        <button
                            type="submit"
                            disabled={isFormUnchanged}
                            className={
                                isFormUnchanged
                                    ? "bg-gray-400 text-white px-6 py-3 rounded-xl transition-all shadow-md cursor-not-allowed"
                                    : "bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-xl transition-all shadow-md"
                            }
                        >
                            Save Changes
                        </button>
                        <button
                            type="button"
                            onClick={() => {
                                const userInput = prompt("Please enter your email address to confirm account deactivation :");
                                if (userInput === formData.email) handleDisableAccount();
                                else alert("Incorrect email address. Action canceled.");
                            }}
                            className="bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-xl transition-all shadow-md"
                        >
                            Deactivate Account
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
        ;
};

interface InputFieldProps {
    id: string;
    label: string;
    type?: string;
    value: string;
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const InputField: React.FC<InputFieldProps> = ({id, label, type = "text", value, onChange}) => (
    <div className="space-y-2">
        <label htmlFor={id} className="text-sm font-medium text-gray-700">{label}</label>
        <input
            id={id}
            type={type}
            name={id}
            placeholder={label}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
            value={value}
            onChange={onChange}
        />
    </div>
);

interface AddressSectionProps {
    title: string;
    address: Address;
    prefix: string;
    handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const AddressSection: React.FC<AddressSectionProps> = ({title, address, prefix, handleChange}) => (
    <fieldset className="space-y-4">
        <legend className="font-medium text-lg text-gray-600">{title}</legend>
        {(["street", "number", "postalCode", "city", "country"] as (keyof Address)[]).map((fieldKey) => (
            <div key={fieldKey} className="space-y-2">
                <label htmlFor={`${prefix}.${fieldKey}`} className="text-sm font-medium text-gray-700">
                    {fieldKey.charAt(0).toUpperCase() + fieldKey.slice(1)}
                </label>
                <input
                    id={`${prefix}.${fieldKey}`}
                    type="text"
                    name={`${prefix}.${fieldKey}`}
                    placeholder={fieldKey.charAt(0).toUpperCase() + fieldKey.slice(1)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                    value={address[fieldKey]}
                    onChange={handleChange}
                />
            </div>
        ))}
    </fieldset>
);

export default Dashboard;
