import {Translation} from "../../translations-payment";

export interface CheckoutFormProps {
    formData: {
        fullName: string;
        email: string;
        phone: string;
        address: string;
        country: string;
        city: string;
        state: string;
        zip: string;
        cardNumber: string;
        cardExpiry: string;
        cardCVC: string;
        termsAccepted: boolean;
    };
    handleInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    formError: string | null;
    translations: Translation;
}