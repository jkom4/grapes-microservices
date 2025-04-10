import { useState, useEffect, createContext, useContext } from "react";
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
    },
    fr: {
        checkout: "Commande",
        fullName: "Nom complet",
        email: "Adresse email",
        phone: "Numéro de téléphone",
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
    }
};

// Main Component
const CartPage = () => {
    const { language } = useLanguage();
    const [cart, setCart] = useState<CartResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [formData, setFormData] = useState({
        fullName: "",
        email: "",
        phone: "",
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
        const {
            fullName,
            email,
            phone,
            country,
            city,
            state,
            zip,
            cardNumber,
            cardExpiry,
            cardCVC,
            termsAccepted,
        } = formData;
        return (
            fullName &&
            email &&
            phone &&
            country &&
            city &&
            state &&
            zip &&
            cardNumber &&
            cardExpiry &&
            cardCVC &&
            termsAccepted
        );
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

            alert("Payment successful!");
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

    if (loading) return <div className="text-center p-8">{translations[language].checkout}...</div>;
    if (error) return <div className="text-center p-8 text-red-600">Error: {error}</div>;
    if (!cart) return <div className="text-center p-8">No items in the cart.</div>;

    return (
        <div className="bg-gray-100 min-h-screen p-6 lg:p-12">
            <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Delivery form and payment */}
                <div className="bg-white rounded-xl p-8 shadow-lg">
                    <h1 className="text-3xl font-bold text-secondary mb-6">{translations[language].checkout}</h1>

                    <form className="space-y-4">
                        <input type="text" name="fullName" placeholder={translations[language].fullName} required className="w-full border border-gray-300 rounded-lg p-3" value={formData.fullName} onChange={handleInputChange} />
                        <input type="email" name="email" placeholder={translations[language].email} required className="w-full border border-gray-300 rounded-lg p-3" value={formData.email} onChange={handleInputChange} />
                        <input type="tel" name="phone" placeholder={translations[language].phone} required className="w-full border border-gray-300 rounded-lg p-3" value={formData.phone} onChange={handleInputChange} />
                        <input type="text" name="country" placeholder={translations[language].country} required className="w-full border border-gray-300 rounded-lg p-3" value={formData.country} onChange={handleInputChange} />

                        <div className="grid grid-cols-3 gap-4">
                            <input type="text" name="city" placeholder={translations[language].city} className="border border-gray-300 rounded-lg p-3" value={formData.city} onChange={handleInputChange} />
                            <input type="text" name="state" placeholder={translations[language].state} className="border border-gray-300 rounded-lg p-3" value={formData.state} onChange={handleInputChange} />
                            <input type="text" name="zip" placeholder={translations[language].zip} className="border border-gray-300 rounded-lg p-3" value={formData.zip} onChange={handleInputChange} />
                        </div>

                        {/* Infos Cart */}
                        <h2 className="text-lg font-semibold mt-6">{translations[language].paymentError}</h2>
                        <input type="text" name="cardNumber" placeholder={translations[language].cardNumber} className="w-full border border-gray-300 rounded-lg p-3" value={formData.cardNumber} onChange={handleInputChange} />
                        <div className="grid grid-cols-2 gap-4">
                            <input type="text" name="cardExpiry" placeholder={translations[language].cardExpiry} className="border border-gray-300 rounded-lg p-3" value={formData.cardExpiry} onChange={handleInputChange} />
                            <input type="text" name="cardCVC" placeholder={translations[language].cardCVC} className="border border-gray-300 rounded-lg p-3" value={formData.cardCVC} onChange={handleInputChange} />
                        </div>

                        <label className="flex items-center space-x-2 text-sm mt-2">
                            <input type="checkbox" name="termsAccepted" checked={formData.termsAccepted} onChange={handleInputChange} />
                            <span>{translations[language].terms}</span>
                        </label>
                    </form>

                    {formError && <div className="mt-4 text-red-600 text-sm">{formError}</div>}
                </div>

                {/* Summary Cart */}
                <div className="bg-primary rounded-xl p-8 shadow-xl">
                    <h2 className="text-xl font-bold text-gray-800 mb-6">{translations[language].titleCart}</h2>

                    <div className="space-y-4 max-h-[280px] overflow-y-auto pr-2">
                        {cart.items.map((item) => (
                            <div key={item.id} className="flex items-center bg-white rounded-lg p-4 shadow-sm">
                                <img
                                    src={item.picturePath || "/default-image.jpg"}
                                    alt={item.articleName}
                                    className="w-16 h-16 object-cover rounded-lg mr-4"
                                />
                                <div className="flex-1">
                                    <h3 className="font-semibold">{item.articleName}</h3>
                                    <p className="text-sm text-gray-500">1x</p>
                                </div>
                                <span className="font-semibold text-gray-800">{item.price.toFixed(2)} €</span>
                            </div>
                        ))}
                    </div>

                    <div className="mt-6 space-y-2 text-sm text-gray-700">
                        <div className="flex justify-between">
                            <span>{translations[language].subtotal}</span>
                            <span>{cart.totalPrice.toFixed(2)} €</span>
                        </div>
                        <div className="flex justify-between">
                            <span>{translations[language].shipping}</span>
                            <span>5.00 €</span>
                        </div>
                        <div className="flex justify-between font-bold text-lg text-gray-800">
                            <span>{translations[language].total}</span>
                            <span>{(cart.totalPrice + 5).toFixed(2)} €</span>
                        </div>
                    </div>

                    <button
                        onClick={handlePayment}
                        disabled={isPaying}
                        className={`w-full py-3 mt-6 rounded-lg transition-all ${isPaying ? "bg-secondary cursor-not-allowed" : "bg-purple-600 hover:bg-purple-700"} text-white font-semibold`}
                    >
                        {isPaying ? translations[language].processing : translations[language].payNow}
                    </button>

                    {/* Payment error */}
                    {paymentError && <div className="mt-4 text-red-600 text-sm">{paymentError}</div>}

                    <div className="mt-4 text-xs text-secondary flex items-start space-x-2">
                        <span className="text-lg">🔒</span>
                        <p>
                            <strong>{translations[language].secureCheckout}</strong><br />
                            Ensuring your financial and personal details are secure during every transaction.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CartPage;
