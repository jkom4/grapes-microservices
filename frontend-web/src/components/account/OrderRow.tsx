import { useLanguage } from "../../features/LanguageContext";
import { Order } from "../../utils/models/interface/Order";
import { translationsAccount } from "../../utils/translations-account";

interface OrderRowProps {
    order: Order;
    translations: typeof translationsAccount["en"];
}

const OrderRow: React.FC<OrderRowProps> = ({ order, translations }) => {
    const { language } = useLanguage();

    const formatDate = (dateString: string): string => {
        return new Date(dateString).toLocaleString(language === "en" ? "en-US" : "fr-FR", {
            dateStyle: "medium",
            timeStyle: "short",
        });
    };

    return (
        <tr className="border-b hover:bg-gray-50">
            <td className="px-6 py-4 text-gray-700">{order.id}</td>
            <td className="px-6 py-4 text-gray-700">{order.code}</td>
            <td className="px-6 py-4 text-gray-700">{formatDate(order.createdAt)}</td>
            <td className="px-6 py-4 text-gray-700">
                {order.totalPrice ? `${translations.currency}${(order.totalPrice + 5).toFixed(2)}` : "-"}
            </td>
            <td className="px-6 py-4">
        <span
            className={`inline-block px-3 py-1 text-sm font-semibold rounded-full ${
                order.paid ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"
            }`}
        >
          {order.paid ? translations.paid : translations.pending}
        </span>
            </td>
            <td className="px-6 py-4">
                {order.facturePath ? (
                    <a
                        href={`http://localhost:3000/uploads${order.facturePath}`}
                        download
                        target="_blank"
                        rel="noopener noreferrer"
                        className="bg-accent text-white px-4 py-2 rounded-md hover:bg-accent transition inline-block"
                    >
                        {translations.view}
                    </a>
                ) : (
                    <span className="text-gray-500">{translations.na}</span>
                )}
            </td>
        </tr>
    );
};

export default OrderRow;