// components/CartPage/CartPage.tsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useLanguage } from "../../features/LanguageContext";
import { cartService, CartResponse } from "../../services/cartService";
import CartItemModel from "../../utils/models/CartItem";
import { translationsPayment } from "../../utils/translations-payment";
import LoadingSpinner from "../../utils/models/interface/LoadSpinner";
import ErrorMessage from "../../utils/models/interface/ErrorMessage";
import SuccessModal from "../../components/cartPage/SuccessModal";
import CartHeader from "../../components/cartPage/CartHeader";
import CheckoutForm from "../../components/cartPage/CheckoutForm";
import CartItems from "../../components/cartPage/CartItems";
import CartSummary from "../../components/cartPage/CartSummary";


const CartPage = () => {
    const { language } = useLanguage();
    const navigate = useNavigate();
    const [cart, setCart] = useState<CartResponse | null>(null);
    const [orderId, setOrderId] = useState<string | null>(null);
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

    // Hardcoded userId for cart initialization
    const userId = 1;

    useEffect(() => {
        const initializeAndFetchCart = async () => {
            try {
                setLoading(true);
                let dynamicOrderId = localStorage.getItem("orderId");

                // If no orderId exists, initialize a new cart
                if (!dynamicOrderId) {
                    const initResponse = await cartService.initializeCart(userId);
                    dynamicOrderId = initResponse.id.toString();
                    localStorage.setItem("orderId", dynamicOrderId);
                }
                setOrderId(dynamicOrderId);

                // Fetch cart with dynamic orderId
                const data = await cartService.fetchCart(dynamicOrderId);
                setCart(data);

                // Clear localStorage if cart is empty
                if (data.items.length === 0) {
                    localStorage.removeItem("orderId");
                    setOrderId(null);
                }
            } catch (err) {
                setError(
                    err instanceof Error
                        ? err.message
                        : "An unknown error occurred while initializing or fetching the cart"
                );
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
        const { fullName, phone, address, termsAccepted } = formData;
        return fullName && phone && address && termsAccepted;
    };

    const handlePayment = async () => {
        if (!orderId) {
            setFormError("Order ID is not available. Please try again.");
            return;
        }

        setFormError(null);
        setPaymentError(null);

        if (!isFormComplete()) {
            setFormError(translationsPayment[language].completeForm);
            return;
        }

        setIsPaying(true);
        try {
            await cartService.processPayment(
                orderId,
                formData.address,
                formData.phone,
                formData.fullName
            );
            setShowSuccess(true);

            setTimeout(async () => {
                try {
                    await cartService.clearCart(orderId);
                    setCart(null);
                    setOrderId(null);
                    localStorage.clear();
                } catch (err) {
                    // Handle error silently to avoid interrupting user flow
                }
                setShowSuccess(false);
                navigate("/");
            }, 5000);
        } catch (err) {
            setPaymentError(
                err instanceof Error
                    ? err.message
                    : translationsPayment[language].paymentError
            );
        } finally {
            setIsPaying(false);
        }
    };

    const handleStripePayment = async () => {
        if (!orderId) {
            setFormError("Order ID is not available. Please try again.");
            return;
        }

        setFormError(null);
        setPaymentError(null);

        if (!isFormComplete()) {
            setFormError(translationsPayment[language].completeForm);
            return;
        }
        window.location.href = "https://buy.stripe.com/test_fZe3cr0INeIK50c6oo";
    };

    const handleRemoveItem = async (orderId: string | null, itemId: number) => {
        if (!orderId) {
            setError("Order ID is not available.");
            return;
        }
        try {
            setError(null);
            await cartService.removeItem(Number(orderId), itemId);
            const updatedCart = await cartService.fetchCart(orderId);
            setCart(updatedCart);
            if (updatedCart.items.length === 0) {
                localStorage.removeItem("orderId");
                setOrderId(null);
            }
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "An unknown error occurred while removing the item"
            );
        }
    };

    const calculateItemPrice = (item: CartItemModel) => {
        return (
            item.quantityKg > 0
                ? item.price * item.quantityKg
                : item.price * item.quantity
        ).toFixed(2);
    };

    const getItemQuantityDisplay = (item: CartItemModel) => {
        return item.quantityKg > 0
            ? `${item.quantityKg} ${translationsPayment[language].kg}`
            : `${item.quantity} ${translationsPayment[language].unit}`;
    };

    const getUnitPriceDisplay = (item: CartItemModel) => {
        return `${item.price.toFixed(2)} €/${
            item.quantityKg > 0
                ? translationsPayment[language].kg
                : translationsPayment[language].unit
        }`;
    };

    const calculateSubtotal = () => {
        if (!cart || cart.items.length === 0) return 0;
        return cart.items.reduce(
            (total, item) =>
                total +
                (item.quantityKg > 0
                    ? item.price * item.quantityKg
                    : item.price * item.quantity),
            0
        );
    };

    if (loading) {
        return <LoadingSpinner message={translationsPayment[language].checkout} />;
    }

    if (error) {
        return <ErrorMessage message={error} />;
    }

    if (!cart) {
        return (
            <div className="text-center p-8">{translationsPayment[language].emptyCart}</div>
        );
    }

    const isCartEmpty = cart.items.length === 0;
    const subtotal = calculateSubtotal();
    const shipping = isCartEmpty ? 0 : 5.0;
    const total = subtotal + shipping;

    return (
        <div className="bg-gray-100 min-h-screen p-6 lg:p-12">
            {showSuccess && (
                <SuccessModal
                    formData={formData}
                    cart={cart}
                    subtotal={subtotal}
                    shipping={shipping}
                    total={total}
                    translations={translationsPayment[language]}
                />
            )}
            <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-8">
                <div className="bg-white rounded-xl p-8 shadow-lg">
                    <CartHeader translations={translationsPayment[language]} />
                    <CheckoutForm
                        formData={formData}
                        handleInputChange={handleInputChange}
                        formError={formError}
                        translations={translationsPayment[language]}
                    />
                </div>
                <div className="bg-primary rounded-xl p-8 shadow-xl">
                    <CartItems
                        cart={cart}
                        orderId={orderId}
                        handleRemoveItem={handleRemoveItem}
                        calculateItemPrice={calculateItemPrice}
                        getItemQuantityDisplay={getItemQuantityDisplay}
                        getUnitPriceDisplay={getUnitPriceDisplay}
                        translations={translationsPayment[language]}
                    />
                    <CartSummary
                        isCartEmpty={isCartEmpty}
                        subtotal={subtotal}
                        shipping={shipping}
                        total={total}
                        isPaying={isPaying}
                        handlePayment={handlePayment}
                        handleStripePayment={handleStripePayment}
                        paymentError={paymentError}
                        translations={translationsPayment[language]}
                    />
                </div>
            </div>
        </div>
    );
};

export default CartPage;