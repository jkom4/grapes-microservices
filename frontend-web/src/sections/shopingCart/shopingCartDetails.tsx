// src/components/CartPage.tsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useLanguage } from "../../features/LanguageContext";
import { cartService, CartResponse } from "../../services/cartService";
import CartItemModel from "../../utils/models/CartItem";
import { translations } from "../../utils/translations";

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
    const [showSuccess, setShowSuccess] = useState(false);

    // Hardcoded orderId for all operations
    const orderId = "1";
    // Hardcoded userId for initializeCart
    const userId = 1;

    useEffect(() => {
        const initializeAndFetchCart = async () => {
            try {
                setLoading(true);
                // Initialize cart with userId
                await cartService.initializeCart(userId);
                // Fetch cart with hardcoded orderId
                const data = await cartService.fetchCart(orderId);
                setCart(data);
            } catch (err) {
                setError(err instanceof Error ? err.message : "An unknown error occurred while initializing or fetching the cart");
            } finally {
                setLoading(false);
            }
        };

        initializeAndFetchCart();
    }, []);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, type, checked } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const isFormComplete = () => {
        const { fullName, email, phone, address, country, city, state, zip, cardNumber, cardExpiry, cardCVC, termsAccepted } =
            formData;
        return (
            fullName &&
            email &&
            phone &&
            address &&
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
            await cartService.processPayment(orderId);
            setShowSuccess(true);

            setTimeout(async () => {
                try {
                    await cartService.clearCart(orderId);
                } catch (err) {
                    console.error("Error clearing cart:", err instanceof Error ? err.message : "Unknown error");
                }
                setShowSuccess(false);
                navigate("/");
            }, 5000);
        } catch (err) {
            setPaymentError(err instanceof Error ? err.message : translations[language].paymentError);
        } finally {
            setIsPaying(false);
        }
    };

    const handleRemoveItem = async (itemId: number) => {
        try {
            await cartService.removeItem(itemId);
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
        } catch (err) {
            setError(err instanceof Error ? err.message : "An unknown error occurred while removing the item");
        }
    };


    const handleAddItem = async (articleId: number, quantityKg: number, quantity: number) => {
        try {
            const updatedCart = await cartService.addItemToCart(parseInt(orderId), articleId, quantityKg, quantity);
            setCart(updatedCart);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to add item to cart");
        }
    };

    const calculateItemPrice = (item: CartItemModel) => {
        return (item.quantityKg > 0 ? item.price * item.quantityKg : item.price * item.quantity).toFixed(2);
    };

    const getItemQuantityDisplay = (item: CartItemModel) => {
        return item.quantityKg > 0
            ? `${item.quantityKg} ${translations[language].kg}`
            : `${item.quantity} ${translations[language].unit}`;
    };

    const getUnitPriceDisplay = (item: CartItemModel) => {
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
            {showSuccess && (
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
                            {translations[language].transactionSummary}
                        </h2>
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
                        <p className="text-gray-600 text-center mt-6">{translations[language].redirecting}</p>
                    </div>
                </div>
            )}
            <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-8">
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
                    <div className="mt-6 space-y-4">
                        <div className="space-y-2 text-sm text-gray-700 border-t pt-4">
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