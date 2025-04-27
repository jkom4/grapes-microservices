import { Order, SortConfig } from "../../utils/models/interface/Order";
import { translationsAccount } from "../../utils/translations-account";
import OrderRow from "./OrderRow";

interface OrderTableProps {
    orders: Order[];
    sortConfig: SortConfig;
    handleSort: (key: keyof Order) => void;
    translations: typeof translationsAccount["en"];
}

const OrderTable: React.FC<OrderTableProps> = ({ orders, sortConfig, handleSort, translations }) => {
    return (
        <div className="overflow-x-auto shadow-md rounded-lg">
            <table className="min-w-full bg-white">
                <thead className="bg-gray-100">
                <tr>
                    <th
                        className="px-6 py-3 text-left text-sm font-semibold text-gray-600 cursor-pointer"
                        onClick={() => handleSort("id")}
                    >
                        {translations.trackingNumber}{" "}
                        {sortConfig.key === "id" && (sortConfig.direction === "asc" ? "↑" : "↓")}
                    </th>
                    <th
                        className="px-6 py-3 text-left text-sm font-semibold text-gray-600 cursor-pointer"
                        onClick={() => handleSort("code")}
                    >
                        {translations.orderCode}{" "}
                        {sortConfig.key === "code" && (sortConfig.direction === "asc" ? "↑" : "↓")}
                    </th>
                    <th
                        className="px-6 py-3 text-left text-sm font-semibold text-gray-600 cursor-pointer"
                        onClick={() => handleSort("createdAt")}
                    >
                        {translations.date}{" "}
                        {sortConfig.key === "createdAt" && (sortConfig.direction === "asc" ? "↑" : "↓")}
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-600">
                        {translations.status}
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-600">
                        {translations.invoice}
                    </th>
                </tr>
                </thead>
                <tbody>
                {orders.map((order) => (
                    <OrderRow key={order.id} order={order} translations={translations} />
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default OrderTable;