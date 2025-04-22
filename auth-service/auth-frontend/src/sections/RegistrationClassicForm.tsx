import React, {useState} from "react";

interface RegistrationFormProps {
    formData: any;
    setFormData: React.Dispatch<React.SetStateAction<any>>;
    handleChange: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void;
    handleAddressChange: (e: React.ChangeEvent<HTMLInputElement>, addressType: "deliveryAddress" | "billingAddress") => void;
    handleSubmit: (e: React.FormEvent, formData: any) => void;
    errors: any;
    loading: boolean;
    error: string | null;
    useSameAddress: boolean;
    setUseSameAddress: React.Dispatch<React.SetStateAction<boolean>>;
}

const RegistrationForm: React.FC<RegistrationFormProps> = ({
                                                               formData,
                                                               handleChange,
                                                               handleAddressChange,
                                                               handleSubmit,
                                                               errors,
                                                               loading,
                                                               error,
                                                               useSameAddress,
                                                               setUseSameAddress,
                                                           }) => {
    const [nationalId, setNationalId] = useState("");
    const [pinCode, setPinCode] = useState(formData.pinCode || "");

    const handleNationalIdChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const raw = e.target.value.replace(/\D/g, "").slice(0, 11);
        const parts = [
            raw.slice(0, 2),
            raw.slice(2, 4),
            raw.slice(4, 6),
            raw.slice(6, 9),
            raw.slice(9, 11),
        ];
        const formattedValue = parts
            .filter(Boolean)
            .map((part, i) => (i === 3 ? "-" + part : i > 0 ? "." + part : part))
            .join("");
        setNationalId(formattedValue);
        handleChange({ target: { name: "nationalId", value: raw } } as React.ChangeEvent<HTMLInputElement>);
    };

    const handlePinCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        if (/^\d{0,4}$/.test(value)) {
            handleChange(e);
        }
    };

    const handlePostalCodeChange = (e: React.ChangeEvent<HTMLInputElement>, addressType: "deliveryAddress" | "billingAddress") => {
        const value = e.target.value;
        if (/^\d*$/.test(value)) {
            handleAddressChange(e, addressType);
        }
    };

    const handlePhoneNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        if (/^\+?\d*$/.test(value)) {
            handleChange(e);
        }
    };

    const handleTextChange = (e: React.ChangeEvent<HTMLInputElement>, name: string) => {
        const value = e.target.value;
        const regex = /^[a-zA-Z\s]*$/;
        if (regex.test(value) || value === "") {
            handleChange(e);
        }
    };

    const handleSubmitForm = (e: React.FormEvent) => {
        e.preventDefault();
        const cleanedNationalId = nationalId.replace(/[^\d]/g, "");

        const updatedFormData = { ...formData, nationalId: cleanedNationalId };

        handleSubmit(e, updatedFormData);
    };
    return (
        <div>
            {loading && <div>Loading...</div>}
            <section className="max-w-6xl mx-auto p-8 bg-white shadow-xl rounded-lg my-8">
                <h2 className="text-3xl font-semibold mb-6 text-gray-800">Register</h2>
                <form onSubmit={handleSubmitForm} className="space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {[
                            { name: "firstName", type: "text", placeholder: "First Name", required: true, regex: /^[a-zA-Z\s]*$/ },
                            { name: "name", type: "text", placeholder: "Last Name", required: true },
                            { name: "email", type: "email", placeholder: "Email", required: true },
                            { name: "password", type: "password", placeholder: "Password", required: true },
                            { name: "birthDate", type: "date", placeholder: "Birth Date", required: true },
                            { name: "phoneNumber", type: "text", placeholder: "Phone Number", required: true },
                            { name: "nationalId", type: "text", placeholder: "National ID", required: true },
                            { name: "pinCode", type: "password", placeholder: "PIN Code", required: true, onChange: handlePinCodeChange, value: pinCode },
                            { name: "profession", type: "text", placeholder: "Profession" },
                        ].map((field) => (
                            <div key={field.name} className="flex flex-col space-y-2">
                                <input
                                    name={field.name}
                                    type={field.type}
                                    placeholder={field.required ? `${field.placeholder} *` : field.placeholder}
                                    required={field.required}
                                    onChange={
                                        ["firstName", "name", "profession"].includes(field.name)
                                            ? (e) => handleTextChange(e, field.name)
                                            : field.name === "nationalId"
                                                ? handleNationalIdChange
                                                : field.name === "pinCode"
                                                    ? handlePinCodeChange
                                                    : field.name === "phoneNumber"
                                                        ? handlePhoneNumberChange
                                                        : handleChange
                                    }
                                    value={field.name === "nationalId" ? nationalId : formData[field.name] || ""}                                    className="p-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                                {errors[field.name] && (
                                    <span className="text-sm text-red-500">{errors[field.name]}</span>
                                )}
                            </div>
                        ))}

                        {/* Gender Select */}
                        <div className="flex flex-col space-y-2">
                            <select
                                name="gender"
                                onChange={handleChange}
                                className="p-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                defaultValue="MALE"
                            >
                                <option value="MALE">Male</option>
                                <option value="FEMALE">Female</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>
                    </div>

                    <hr className="my-6" />

                    <h3 className="text-xl font-semibold mb-4 text-gray-800">Delivery Address</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {Object.keys(formData.deliveryAddress).map((field) => (
                            <input
                                key={field}
                                name={field}
                                placeholder={`${field.charAt(0).toUpperCase() + field.slice(1)} *`}
                                required
                                onChange={
                                    field === "postalCode"
                                        ? (e) => handlePostalCodeChange(e, "deliveryAddress")
                                        : (e) => handleAddressChange(e, "deliveryAddress")
                                }                                className="p-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        ))}
                    </div>

                    <div className="flex items-center gap-2 mt-4">
                        <input
                            type="checkbox"
                            checked={useSameAddress}
                            onChange={() => setUseSameAddress(!useSameAddress)}
                            id="sameAddress"
                            className="text-blue-600"
                        />
                        <label htmlFor="sameAddress" className="text-gray-700">Billing address is the same as delivery</label>
                    </div>

                    {!useSameAddress && (
                        <>
                            <h3 className="text-xl font-semibold mt-6 mb-4 text-gray-800">Billing Address</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                {Object.keys(formData.billingAddress).map((field) => (
                                    <input
                                        key={field}
                                        name={field}
                                        placeholder={field.charAt(0).toUpperCase() + field.slice(1)}
                                        onChange={(e) => handleAddressChange(e, "billingAddress")}
                                        className="p-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    />
                                ))}
                            </div>
                        </>
                    )}

                    <button type="submit" className="w-full py-3 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 mt-6 focus:outline-none focus:ring-2 focus:ring-blue-500">
                        Register
                    </button>
                </form>
                {error && <div className="mt-4 text-red-500 text-center">{error}</div>}
            </section>
        </div>
    );
};

export default RegistrationForm;
