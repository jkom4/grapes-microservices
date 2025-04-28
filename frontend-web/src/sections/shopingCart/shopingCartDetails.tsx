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
import { useCart } from "../../features/CartContext";
import { toast } from "react-toastify";

const CartPage = () => {
    const { language } = useLanguage();
    const navigate = useNavigate();
    const { orderId, setOrderId, initializeCart } = useCart();

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
    const [paymentCompleted, setPaymentCompleted] = useState(false);

    // Check login status like in Navbar
    const isLoggedIn = sessionStorage.getItem("accessToken") !== null;

    useEffect(() => {
        const fetchCart = async () => {
            try {
                setLoading(true);
                if (orderId !== null) {
                    const data = await cartService.fetchCart(orderId);
                    setCart(data);

                    if (data.items.length === 0) {
                        localStorage.removeItem("orderId");
                        setOrderId(null);
                    }
                } else {
                    setCart(null);
                }
            } catch (err) {
                setError(
                    err instanceof Error
                        ? err.message
                        : translationsPayment[language].errorFetchingCart || "Error to fetch cart"
                );
            } finally {
                setLoading(false);
            }
        };

        fetchCart();

        return () => {
            setPaymentCompleted(false);
        };
    }, [orderId, paymentCompleted, setOrderId]);

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
        // Check if user is logged in
        if (!isLoggedIn) {
            toast.error(
                translationsPayment[language].loginRequired ||
                "You must be logged in to proceed with payment.",
                {
                    position: "top-right",
                    autoClose: 3000,
                    hideProgressBar: false,
                    closeOnClick: true,
                    pauseOnHover: true,
                    draggable: true,
                    theme: "colored",
                }
            );
            navigate("/login");
            return;
        }

        if (orderId === null) {
            setFormError(translationsPayment[language].invalidOrderId || "ID isn't free");
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
            const paymentData = {
                orderId,
                address: formData.address,
                phoneNumber: formData.phone,
                customerName: formData.fullName,
                amount: total,
            };

            const providerRedirectUrl = await cartService.processPayment(
                paymentData.orderId,
                paymentData.address,
                paymentData.phoneNumber,
                paymentData.customerName,
                paymentData.amount
            );


            setCart(null);
            localStorage.removeItem("orderId");
            await initializeCart();
            window.location.href = providerRedirectUrl;
        } catch (err) {
            setPaymentError(
                err instanceof Error
                    ? err.message
                    : translationsPayment[language].paymentError || "Error processing payment"
            );
        } finally {
            setIsPaying(false);
        }
    };

    const handleStripePayment = async () => {
        // Check if user is logged in
        if (!isLoggedIn) {
            toast.error(
                translationsPayment[language].loginRequired ||
                "You must be logged in to proceed with payment.",
                {
                    position: "top-right",
                    autoClose: 3000,
                    hideProgressBar: false,
                    closeOnClick: true,
                    pauseOnHover: true,
                    draggable: true,
                    theme: "colored",
                }
            );
            navigate("/login"); // Redirect to login page
            return;
        }

        if (orderId === null) {
            setFormError(translationsPayment[language].invalidOrderId || "Error, retry");
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

    const handleRemoveItem = async (orderId: number | null, itemId: number) => {
        if (orderId === null) {
            setError(translationsPayment[language].invalidOrderId || "ID isn't free");
            return;
        }
        try {
            setError(null);
            await cartService.removeItem(orderId, itemId);
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
                    : translationsPayment[language].errorRemovingItem || "Error to delete an article"
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
                        isLoggedIn={isLoggedIn}
                    />
                </div>
            </div>
        </div>
    );
};

export default CartPage;