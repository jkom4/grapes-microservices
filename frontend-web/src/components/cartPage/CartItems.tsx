import { CartItemsProps } from "../../utils/models/interface/CartItemProps";

const CartItems: React.FC<CartItemsProps> = ({
                                                 cart,
                                                 orderId,
                                                 handleRemoveItem,
                                                 calculateItemPrice,
                                                 getItemQuantityDisplay,
                                                 getUnitPriceDisplay,
                                                 translations,
                                             }) => {
    const isCartEmpty = cart.items.length === 0;

    return (
        <div>
            <h2 className="text-xl font-bold text-gray-800 mb-6">{translations.titleCart}</h2>
            {isCartEmpty ? (
                <div className="text-red-600 text-center p-4">{translations.emptyCart}</div>
            ) : (
                <div className="space-y-4 max-h-[280px] overflow-y-auto pr-2">
                    {cart.items.map((item) => (
                        <div
                            key={item.id}
                            className="flex items-center bg-gray-50 rounded-lg p-4 shadow-sm"
                        >
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
                                onClick={() => orderId && handleRemoveItem(orderId, item.id)}
                                disabled={!orderId}
                                className={`ml-4 text-red-600 hover:text-red-800 transition ${
                                    !orderId ? "opacity-50 cursor-not-allowed" : ""
                                }`}
                                aria-label={`Remove ${item.articleName} from cart`}
                            >
                                🗑️
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default CartItems;