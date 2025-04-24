import {CartResponse} from "../../../services/cartService";
import {Translation} from "../../translations-payment";

export interface SuccessModalProps {
    formData: {
        fullName: string;
        email: string;
        phone: string;
        address: string;
    };
    cart: CartResponse;
    subtotal: number;
    shipping: number;
    total: number;
    translations: Translation;
}