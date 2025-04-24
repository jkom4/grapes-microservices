import { CheckoutFormProps } from "../../utils/models/interface/CheckoutFormProps";

const CheckoutForm: React.FC<CheckoutFormProps> = ({
                                                       formData,
                                                       handleInputChange,
                                                       formError,
                                                       translations,
                                                   }) => {
    return (
        <div>
            <div className="space-y-4">
                <input
                    type="text"
                    name="fullName"
                    placeholder={translations.fullName}
                    required
                    className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                    value={formData.fullName}
                    onChange={handleInputChange}
                />
                <input
                    type="email"
                    name="email"
                    placeholder={translations.email}
                    className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                    value={formData.email}
                    onChange={handleInputChange}
                />
                <input
                    type="tel"
                    name="phone"
                    placeholder={translations.phone}
                    required
                    className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                    value={formData.phone}
                    onChange={handleInputChange}
                />
                <input
                    type="text"
                    name="address"
                    placeholder={translations.address}
                    required
                    className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                    value={formData.address}
                    onChange={handleInputChange}
                />
                <h2 className="text-lg font-semibold mt-6 text-gray-800">{translations.payNow}</h2>
                <input
                    type="text"
                    name="cardNumber"
                    placeholder={translations.cardNumber}
                    className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                    value={formData.cardNumber}
                    onChange={handleInputChange}
                />
                <div className="grid grid-cols-2 gap-4">
                    <input
                        type="text"
                        name="cardExpiry"
                        placeholder={translations.cardExpiry}
                        className="border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                        value={formData.cardExpiry}
                        onChange={handleInputChange}
                    />
                    <input
                        type="text"
                        name="cardCVC"
                        placeholder={translations.cardCVC}
                        className="border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                        value={formData.cardCVC}
                        onChange={handleInputChange}
                    />
                </div>
                <label className="flex items-center space-x-2 text-sm mt-2 text-gray-600">
                    <input
                        type="checkbox"
                        name="termsAccepted"
                        checked={formData.termsAccepted}
                        onChange={handleInputChange}
                        className="h-4 w-4 text-accent border-gray-300 rounded focus:ring-accent"
                    />
                    <span>{translations.terms}</span>
                </label>
            </div>
            {formError && <div className="mt-4 text-red-600 text-sm">{formError}</div>}
        </div>
    );
};

export default CheckoutForm;