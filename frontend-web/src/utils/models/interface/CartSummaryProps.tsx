import {Translation} from "../../translations-payment";

export interface CartSummaryProps {
    isCartEmpty: boolean;
    subtotal: number;
    shipping: number;
    total: number;
    isPaying: boolean;
    handlePayment: () => void;
    handleStripePayment: () => void;
    paymentError: string | null;
    translations: Translation;
}