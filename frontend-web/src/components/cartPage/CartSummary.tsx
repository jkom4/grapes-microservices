import { CartSummaryProps } from "../../utils/models/interface/CartSummaryProps";

const CartSummary: React.FC<CartSummaryProps> = ({
                                                     isCartEmpty,
                                                     subtotal,
                                                     shipping,
                                                     total,
                                                     isPaying,
                                                     handlePayment,
                                                     handleStripePayment,
                                                     paymentError,
                                                     translations,
                                                     isLoggedIn,
                                                 }) => {
    return (
        <div className="mt-6 space-y-4">
            <div className="space-y-2 text-sm text-gray-700 border-t pt-4">
                <div className="flex justify-between">
                    <span>{translations.subtotal}</span>
                    <span>{subtotal.toFixed(2)} €</span>
                </div>
                {!isCartEmpty && (
                    <div className="flex justify-between">
                        <span>{translations.shipping}</span>
                        <span>{shipping.toFixed(2)} €</span>
                    </div>
                )}
                <div className="flex justify-between font-bold text-lg text-gray-800">
                    <span>{translations.total}</span>
                    <span>{total.toFixed(2)} €</span>
                </div>
            </div>
            <button
                onClick={handlePayment}
                disabled={isPaying || isCartEmpty || !isLoggedIn}
                className={`w-full py-3 mt-6 rounded-lg transition-all ${
                    isPaying || isCartEmpty || !isLoggedIn
                        ? "bg-gray-400 cursor-not-allowed"
                        : "bg-secondary hover:bg-accent"
                } text-white font-semibold`}
                title={!isLoggedIn ? translations.loginRequired : ""}
            >
                {isPaying ? translations.processing : translations.payWith3DSecure || "Payer avec 3D Secure"}
            </button>
            <button
                onClick={handleStripePayment}
                disabled={isPaying || isCartEmpty || !isLoggedIn}
                className={`w-full py-3 rounded-lg transition-all ${
                    isPaying || isCartEmpty || !isLoggedIn
                        ? "bg-gray-400 cursor-not-allowed"
                        : "bg-blue-600 hover:bg-blue-700"
                } text-white font-semibold`}
                title={!isLoggedIn ? translations.loginRequired : ""}
            >
                {isPaying ? translations.processing : translations.payWithStripe || "Payer avec Stripe"}
            </button>
            {paymentError && <div className="mt-4 text-red-600 text-sm">{paymentError}</div>}
            <div className="mt-4 text-xs text-gray-600 flex items-start space-x-2">
                <span className="text-lg">🔒</span>
                <p>
                    <strong>{translations.secureCheckout}</strong>
                    <br />
                    {translations.secureCheckoutDescription ||
                        "Ensuring your financial and personal details are secure during every transaction."}
                </p>
            </div>
        </div>
    );
};

export default CartSummary;