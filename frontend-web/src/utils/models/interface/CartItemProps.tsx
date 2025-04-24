import {CartResponse} from "../../../services/cartService";
import CartItemModel from "../CartItem";
import {Translation} from "../../translations-payment";

export interface CartItemsProps {
    cart: CartResponse;
    orderId: number | null;
    handleRemoveItem: (orderId: number | null, itemId: number) => void;
    calculateItemPrice: (item: CartItemModel) => string;
    getItemQuantityDisplay: (item: CartItemModel) => string;
    getUnitPriceDisplay: (item: CartItemModel) => string;
    translations: Translation;
}