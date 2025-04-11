import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useLanguage } from "../../features/LanguageContext";
import CartItem from "../../utils/models/CartItem";

interface CartResponse {
    items: CartItem[];
    totalPrice: number;
}

const translations = {
    en: {
        checkout: "Checkout",
        fullName: "Full name",
        email: "Email address",
        phone: "Phone number",
        address: "Street address",
        country: "Country",
        city: "City",
        state: "State",
        zip: "ZIP Code",
        cardNumber: "Card Number",
        cardExpiry: "MM/YY",
        cardCVC: "CVC",
        terms: "I have read and agree to the Terms and Conditions.",
        paymentError: "Payment failed. Please try again.",
        completeForm: "Please complete all required fields before paying.",
        secureCheckout: "Secure Checkout – SSL Encrypted",
        payNow: "Pay Now",
        processing: "Processing...",
        subtotal: "Subtotal",
        shipping: "Shipping",
        total: "Total",
        titleCart: "Review your cart",
        emptyCart: "Your cart is empty. Add items to proceed with checkout.",
        promoCode: "Promo Code",
        applyPromo: "Apply",
        promoSuccess: "Promo code applied successfully!",
        promoError: "Invalid promo code. Please try again.",
        kg: "kg",
        unit: "unit",
        paymentSuccess: "Payment Successful!",
        transactionSummary: "Transaction Summary",
        shippingAddress: "Shipping Address",
        itemsOrdered: "Items Ordered",
        redirecting: "Redirecting to homepage...",
    },
    fr: {
        checkout: "Commande",
        fullName: "Nom complet",
        email: "Adresse email",
        phone: "Numéro de téléphone",
        address: "Adresse postale",
        country: "Pays",
        city: "Ville",
        state: "État",
        zip: "Code postal",
        cardNumber: "Numéro de carte",
        cardExpiry: "MM/AA",
        cardCVC: "CVC",
        terms: "J'ai lu et j'accepte les conditions générales.",
        paymentError: "Échec du paiement. Veuillez réessayer.",
        completeForm: "Veuillez remplir tous les champs requis avant de payer.",
        secureCheckout: "Paiement sécurisé – Chiffrement SSL",
        payNow: "Payer maintenant",
        processing: "En cours...",
        subtotal: "Sous-total",
        shipping: "Expédition",
        total: "Total",
        titleCart: "Vérifiez votre panier",
        emptyCart: "Votre panier est vide. Ajoutez des articles pour continuer.",
        promoCode: "Code promo",
        applyPromo: "Appliquer",
        promoSuccess: "Code promo appliqué avec succès !",
        promoError: "Code promo invalide. Veuillez réessayer.",
        kg: "kg",
        unit: "unité",
        paymentSuccess: "Paiement réussi !",
        transactionSummary: "Résumé de la transaction",
        shippingAddress: "Adresse de livraison",
        itemsOrdered: "Articles commandés",
        redirecting: "Redirection vers la page d'accueil...",
    },
};

const CartPage = () => {
    const { language } = useLanguage();
    const navigate = useNavigate();
    const [cart, setCart] = useState<CartResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [formData, setFormData] = useState({
        fullName: "",
        email: "",
        phone: "",
        address: "",
        country: "",
        city: "",
        state: "",
        zip: "",
        cardNumber: "",
        cardExpiry: "",
        cardCVC: "",
        termsAccepted: false,
    });
    const [formError, setFormError] = useState<string | null>(null);
    const [isPaying, setIsPaying] = useState(false);
    const [paymentError, setPaymentError] = useState<string | null>(null);
    const [promoCode, setPromoCode] = useState("");
    const [promoError, setPromoError] = useState<string | null>(null);
    const [promoSuccess, setPromoSuccess] = useState<string | null>(null);
    const [showSuccess, setShowSuccess] = useState(false);

    const orderId = "1";

    useEffect(() => {
        const fetchCart = async () => {
            try {
                const response = await fetch(`http://localhost:8092/clm/cart/${orderId}`);
                if (!response.ok) {
                    const errorDetails = await response.text();
                    throw new Error(`Failed to fetch cart items. Details: ${errorDetails}`);
                }
                const data: CartResponse = await response.json();
                setCart(data);
            } catch (err: unknown) {
                if (err instanceof Error) {
                    setError(err.message);
                } else {
                    setError("An unknown error occurred");
                }
            } finally {
                setLoading(false);
            }
        };

        fetchCart();
    }, [orderId]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, type, checked } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const isFormComplete = () => {
        const { fullName, email, phone, address, country, city, state, zip, cardNumber, cardExpiry, cardCVC, termsAccepted } = formData;
        return fullName && email && phone && address && country && city && state && zip && cardNumber && cardExpiry && cardCVC && termsAccepted;
    };

    const handlePayment = async () => {
        setFormError(null);
        setPaymentError(null);

        if (!isFormComplete()) {
            setFormError(translations[language].completeForm);
            return;
        }

        setIsPaying(true);
        try {
            const response = await fetch(`http://localhost:8092/clm/cart/pay/${orderId}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
            });

            if (!response.ok) {
                const errorDetails = await response.text();
                throw new Error(`Payment failed. Details: ${errorDetails}`);
            }

            setShowSuccess(true);

            setTimeout(async () => {
                try {
                    const clearResponse = await fetch(`http://localhost:8092/clm/cart/clear/${orderId}`, {
                        method: "DELETE",
                        headers: {
                            "Content-Type": "application/json",
                        },
                    });

                    if (!clearResponse.ok) {
                        const errorDetails = await clearResponse.text();
                        console.error(`Failed to clear cart. Details: ${errorDetails}`);
                        // Continue with redirect even if clearing fails to avoid blocking user flow
                    }
                } catch (err: unknown) {
                    console.error("Error clearing cart:", err instanceof Error ? err.message : "Unknown error");
                    // Continue with redirect regardless of error
                }

                setShowSuccess(false);
                navigate("/"); // Redirect to homepage
            }, 5000); // 5 seconds delay
        } catch (err: unknown) {
            if (err instanceof Error) {
                setPaymentError(err.message);
            } else {
                setPaymentError(translations[language].paymentError);
            }
        } finally {
            setIsPaying(false);
        }
    };

    const handleRemoveItem = async (itemId: number) => {
        try {
            const response = await fetch(`http://localhost:8092/clm/cart/remove/${itemId}`, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                },
            });

            if (!response.ok) {
                const errorDetails = await response.text();
                throw new Error(`Failed to remove item. Details: ${errorDetails}`);
            }

            setCart((prevCart) => {
                if (!prevCart) return null;
                const updatedItems = prevCart.items.filter((item) => item.id !== itemId);
                const updatedTotalPrice = updatedItems.reduce(
                    (total, item) => total + (item.quantityKg > 0 ? item.price * item.quantityKg : item.price * item.quantity),
                    0
                );
                return {
                    ...prevCart,
                    items: updatedItems,
                    totalPrice: updatedTotalPrice,
                };
            });
            setPromoCode("");
            setPromoSuccess(null);
            setPromoError(null);
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError("An unknown error occurred while removing the item");
            }
        }
    };

    const handleApplyPromo = async () => {
        if (!promoCode.trim()) return;
        setPromoError(null);
        setPromoSuccess(null);

        try {
            const response = await fetch(`http://localhost:8092/clm/cart/apply-promo`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ orderId, promoCode }),
            });

            if (!response.ok) {
                const errorDetails = await response.text();
                throw new Error(`Failed to apply promo code. Details: ${errorDetails}`);
            }

            const data: CartResponse = await response.json();
            setCart(data);
            setPromoSuccess(translations[language].promoSuccess);
        } catch (err: unknown) {
            if (err instanceof Error) {
                setPromoError(err.message);
            } else {
                setPromoError(translations[language].promoError);
            }
        }
    };

    const calculateItemPrice = (item: CartItem) => {
        if (item.quantityKg > 0) {
            return (item.price * item.quantityKg).toFixed(2);
        } else {
            return (item.price * item.quantity).toFixed(2);
        }
    };

    const getItemQuantityDisplay = (item: CartItem) => {
        if (item.quantityKg > 0) {
            return `${item.quantityKg} ${translations[language].kg}`;
        } else {
            return `${item.quantity} ${translations[language].unit}`;
        }
    };

    const getUnitPriceDisplay = (item: CartItem) => {
        return `${item.price.toFixed(2)} €/${item.quantityKg > 0 ? translations[language].kg : translations[language].unit}`;
    };

    const calculateSubtotal = () => {
        if (!cart || cart.items.length === 0) return 0;
        return cart.items.reduce(
            (total, item) => total + (item.quantityKg > 0 ? item.price * item.quantityKg : item.price * item.quantity),
            0
        );
    };

    if (loading) return <div className="text-center p-8">{translations[language].checkout}...</div>;
    if (error) return <div className="text-center p-8 text-red-600">Error: {error}</div>;
    if (!cart) return <div className="text-center p-8">{translations[language].emptyCart}</div>;

    const isCartEmpty = cart.items.length === 0;
    const subtotal = calculateSubtotal();
    const shipping = isCartEmpty ? 0 : 5.0;
    const total = subtotal + shipping;

    return (
        <div className="bg-gray-100 min-h-screen p-6 lg:p-12">
            {/* Transaction Summary Modal */}
            {showSuccess && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl p-8 shadow-2xl w-full max-w-2xl animate-fade-in">
                        {/* Success Animation */}
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
                        <h2 className="text-2xl font-bold text-gray-800 text-center mb-6">{translations[language].transactionSummary}</h2>

                        {/* Shipping Address */}
                        <div className="mb-6">
                            <h3 className="text-lg font-semibold text-gray-800">{translations[language].shippingAddress}</h3>
                            <div className="mt-2 text-gray-600 space-y-1">
                                <p>{formData.fullName}</p>
                                <p>{formData.email}</p>
                                <p>{formData.phone}</p>
                                <p>{formData.address}</p>
                                <p>
                                    {formData.city}, {formData.state} {formData.zip}
                                </p>
                                <p>{formData.country}</p>
                            </div>
                        </div>

                        {/* Items Ordered */}
                        <div className="mb-6">
                            <h3 className="text-lg font-semibold text-gray-800">{translations[language].itemsOrdered}</h3>
                            <div className="mt-2 space-y-2 max-h-64 overflow-y-auto">
                                {cart.items.map((item) => (
                                    <div key={item.id} className="flex justify-between text-gray-600">
                                        <div>
                                            <p className="font-medium">{item.articleName}</p>
                                            <p className="text-sm">{getItemQuantityDisplay(item)}</p>
                                        </div>
                                        <p>{calculateItemPrice(item)} €</p>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Pricing Summary */}
                        <div className="border-t pt-4">
                            <div className="flex justify-between text-gray-600">
                                <span>{translations[language].subtotal}</span>
                                <span>{subtotal.toFixed(2)} €</span>
                            </div>
                            <div className="flex justify-between text-gray-600 mt-1">
                                <span>{translations[language].shipping}</span>
                                <span>{shipping.toFixed(2)} €</span>
                            </div>
                            <div className="flex justify-between font-bold text-gray-800 mt-2">
                                <span>{translations[language].total}</span>
                                <span>{total.toFixed(2)} €</span>
                            </div>
                        </div>

                        {/* Redirect Notice */}
                        <p className="text-gray-600 text-center mt-6">{translations[language].redirecting}</p>
                    </div>
                </div>
            )}

            <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Delivery form and payment */}
                <div className="bg-white rounded-xl p-8 shadow-lg">
                    <h1 className="text-3xl font-bold text-secondary mb-6">{translations[language].checkout}</h1>

                    <form className="space-y-4">
                        <input
                            type="text"
                            name="fullName"
                            placeholder={translations[language].fullName}
                            required
                            className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                            value={formData.fullName}
                            onChange={handleInputChange}
                        />
                        <input
                            type="email"
                            name="email"
                            placeholder={translations[language].email}
                            required
                            className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                            value={formData.email}
                            onChange={handleInputChange}
                        />
                        <input
                            type="tel"
                            name="phone"
                            placeholder={translations[language].phone}
                            required
                            className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                            value={formData.phone}
                            onChange={handleInputChange}
                        />
                        <input
                            type="text"
                            name="address"
                            placeholder={translations[language].address}
                            required
                            className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                            value={formData.address}
                            onChange={handleInputChange}
                        />
                        <input
                            type="text"
                            name="country"
                            placeholder={translations[language].country}
                            required
                            className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                            value={formData.country}
                            onChange={handleInputChange}
                        />
                        <div className="grid grid-cols-3 gap-4">
                            <input
                                type="text"
                                name="city"
                                placeholder={translations[language].city}
                                className="border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                                value={formData.city}
                                onChange={handleInputChange}
                            />
                            <input
                                type="text"
                                name="state"
                                placeholder={translations[language].state}
                                className="border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                                value={formData.state}
                                onChange={handleInputChange}
                            />
                            <input
                                type="text"
                                name="zip"
                                placeholder={translations[language].zip}
                                className="border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                                value={formData.zip}
                                onChange={handleInputChange}
                            />
                        </div>

                        {/* Payment Info */}
                        <h2 className="text-lg font-semibold mt-6 text-gray-800">{translations[language].payNow}</h2>
                        <input
                            type="text"
                            name="cardNumber"
                            placeholder={translations[language].cardNumber}
                            className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                            value={formData.cardNumber}
                            onChange={handleInputChange}
                        />
                        <div className="grid grid-cols-2 gap-4">
                            <input
                                type="text"
                                name="cardExpiry"
                                placeholder={translations[language].cardExpiry}
                                className="border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-accent focus:border-transparent"
                                value={formData.cardExpiry}
                                onChange={handleInputChange}
                            />
                            <input
                                type="text"
                                name="cardCVC"
                                placeholder={translations[language].cardCVC}
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
                            <span>{translations[language].terms}</span>
                        </label>
                    </form>

                    {formError && <div className="mt-4 text-red-600 text-sm">{formError}</div>}
                </div>

                {/* Summary Cart */}
                <div className="bg-primary rounded-xl p-8 shadow-xl">
                    <h2 className="text-xl font-bold text-gray-800 mb-6">{translations[language].titleCart}</h2>

                    {isCartEmpty ? (
                        <div className="text-red-600 text-center p-4">{translations[language].emptyCart}</div>
                    ) : (
                        <div className="space-y-4 max-h-[280px] overflow-y-auto pr-2">
                            {cart.items.map((item) => (
                                <div key={item.id} className="flex items-center bg-gray-50 rounded-lg p-4 shadow-sm">
                                    <img
                                        src={item.picturePath || "/default-image.jpg"}
                                        alt={item.articleName}
                                        className="w-16 h-16 object-cover rounded-lg mr-4"
                                    />
                                    <div className="flex-1">
                                        <h3 className="font-semibold text-gray-800">{item.articleName}</h3>
                                        <p className="text-sm text-gray-500">
                                            {getItemQuantityDisplay(item)} - {getUnitPriceDisplay(item)}
                                        </p>
                                    </div>
                                    <span className="font-semibold text-gray-800">{calculateItemPrice(item)} €</span>
                                    <button
                                        onClick={() => handleRemoveItem(item.id)}
                                        className="ml-4 text-red-600 hover:text-red-800 transition"
                                        aria-label={`Remove ${item.articleName} from cart`}
                                    >
                                        🗑️
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}

                    <div className="mt-6 space-y-2 text-sm text-gray-700 border-t pt-4">
                        <div className="flex justify-between">
                            <span>{translations[language].subtotal}</span>
                            <span>{subtotal.toFixed(2)} €</span>
                        </div>
                        {!isCartEmpty && (
                            <div className="flex justify-between">
                                <span>{translations[language].shipping}</span>
                                <span>{shipping.toFixed(2)} €</span>
                            </div>
                        )}
                        <div className="flex justify-between font-bold text-lg text-gray-800">
                            <span>{translations[language].total}</span>
                            <span>{total.toFixed(2)} €</span>
                        </div>
                    </div>

                    <button
                        onClick={handlePayment}
                        disabled={isPaying || isCartEmpty}
                        className={`w-full py-3 mt-6 rounded-lg transition-all ${
                            isPaying || isCartEmpty ? "bg-gray-400 cursor-not-allowed" : "bg-secondary hover:bg-accent"
                        } text-white font-semibold`}
                    >
                        {isPaying ? translations[language].processing : translations[language].payNow}
                    </button>

                    {paymentError && <div className="mt-4 text-red-600 text-sm">{paymentError}</div>}

                    <div className="mt-4 text-xs text-gray-600 flex items-start space-x-2">
                        <span className="text-lg">🔒</span>
                        <p>
                            <strong>{translations[language].secureCheckout}</strong>
                            <br />
                            Ensuring your financial and personal details are secure during every transaction.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CartPage;