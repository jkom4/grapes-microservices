import { SuccessModalProps } from "../../utils/models/interface/SuccessModalProps";

const SuccessModal: React.FC<SuccessModalProps> = ({
                                                       formData,
                                                       cart,
                                                       subtotal,
                                                       shipping,
                                                       total,
                                                       translations,
                                                   }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-xl p-8 shadow-2xl w-full max-w-2xl animate-fade-in">
                <div className="relative w-20 h-20 mx-auto mb-6">
                    <div className="absolute inset-0 bg-green-500 rounded-full animate-circle"></div>
                    <svg
                        className="absolute inset-0 w-full h-full text-white animate-checkmark"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    >
                        <path d="M20 6L9 17l-5-5" />
                    </svg>
                </div>
                <h2 className="text-2xl font-bold text-gray-800 text-center mb-6">
                    {translations.transactionSummary}
                </h2>
                <div className="mb-6">
                    <h3 className="text-lg font-semibold text-gray-800">{translations.shippingAddress}</h3>
                    <div className="mt-2 text-gray-600 space-y-1">
                        <p>{formData.fullName}</p>
                        <p>{formData.email}</p>
                        <p>{formData.phone}</p>
                        <p>{formData.address}</p>
                    </div>
                </div>
                <div className="mb-6">
                    <h3 className="text-lg font-semibold text-gray-800">{translations.itemsOrdered}</h3>
                    <div className="mt-2 space-y-2 max-h-64 overflow-y-auto">
                        {cart.items.map((item) => (
                            <div key={item.id} className="flex justify-between text-gray-600">
                                <div>
                                    <p className="font-medium">{item.articleName}</p>
                                    <p className="text-sm">
                                        {item.quantityKg > 0
                                            ? `${item.quantityKg} ${translations.kg}`
                                            : `${item.quantity} ${translations.unit}`}
                                    </p>
                                </div>
                                <p>
                                    {(item.quantityKg > 0
                                            ? item.price * item.quantityKg
                                            : item.price * item.quantity
                                    ).toFixed(2)}{" "}
                                    €
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
                <div className="border-t pt-4">
                    <div className="flex justify-between text-gray-600">
                        <span>{translations.subtotal}</span>
                        <span>{subtotal.toFixed(2)} €</span>
                    </div>
                    <div className="flex justify-between text-gray-600 mt-1">
                        <span>{translations.shipping}</span>
                        <span>{shipping.toFixed(2)} €</span>
                    </div>
                    <div className="flex justify-between font-bold text-gray-800 mt-2">
                        <span>{translations.total}</span>
                        <span>{total.toFixed(2)} €</span>
                    </div>
                </div>
                <p className="text-gray-600 text-center mt-6">{translations.redirecting}</p>
            </div>
        </div>
    );
};

export default SuccessModal;