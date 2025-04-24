import { useEffect, useState } from "react";
import { Order, SortConfig } from "../../utils/models/interface/Order";
import accountService from "../../services/accountService";
import { useLanguage } from "../../features/LanguageContext";
import { translationsAccount } from "../../utils/translations-account";
import {enUS, fr} from "date-fns/locale";
import { format } from "date-fns";
import LoadingSpinner from "../../utils/models/interface/LoadSpinner";
import ErrorMessage from "../../utils/models/interface/ErrorMessage";
import SearchBar from "../../components/account/SearchBar";
import OrderTable from "../../components/account/OrderTable";
import Pagination from "../../components/account/Pagination";


const OrderHistory: React.FC = () => {
    const { language } = useLanguage();
    const [orders, setOrders] = useState<Order[]>([]);
    const [filteredOrders, setFilteredOrders] = useState<Order[]>([]);
    const [sortConfig, setSortConfig] = useState<SortConfig>({ key: "createdAt", direction: "desc" });
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [searchCode, setSearchCode] = useState<string>("");
    const [searchDateTime, setSearchDateTime] = useState<string>("");
    const [currentPage, setCurrentPage] = useState<number>(1);
    const ordersPerPage = 20;

    // Fetch orders
    useEffect(() => {
        const fetchOrders = async () => {
            try {
                const userId = 1; // Replace with logic to get user ID
                const data = await accountService.fetchOrderHistory(userId);
                setOrders(data);
                setFilteredOrders(data);
                setLoading(false);
            } catch (err) {
                setError(
                    err instanceof Error
                        ? err.message
                        : translationsAccount[language].error.replace("{error}", "An error occurred")
                );
                setLoading(false);
            }
        };
        fetchOrders();
    }, [language]);

    // Filter orders
    useEffect(() => {
        const locale = language === "en" ? enUS : fr;
        const filtered = orders.filter((order) => {
            const codeMatch = searchCode
                ? order.code.toString().toLowerCase().includes(searchCode.toLowerCase())
                : true;

            let dateTimeMatch = true;
            if (searchDateTime) {
                const orderDate = new Date(order.createdAt);
                const formattedDateFull = format(orderDate, "PPp", { locale });
                const formattedDateShort = format(orderDate, "PP", { locale });
                const formattedYear = format(orderDate, "yyyy", { locale });
                const formattedMonthYear = format(orderDate, "MMMM yyyy", { locale });
                const formattedDayMonth = format(orderDate, "d MMMM", { locale });
                const formattedTime = format(orderDate, "HH:mm", { locale });

                dateTimeMatch = [
                    formattedDateFull,
                    formattedDateShort,
                    formattedYear,
                    formattedMonthYear,
                    formattedDayMonth,
                    formattedTime,
                ].some((formatted) => formatted.toLowerCase().includes(searchDateTime.toLowerCase()));
            }

            return codeMatch && dateTimeMatch;
        });
        setFilteredOrders(filtered);
        setCurrentPage(1);
    }, [orders, searchCode, searchDateTime, language]);

    // Sorting logic
    const handleSort = (key: keyof Order) => {
        let direction: "asc" | "desc" = "asc";
        if (sortConfig.key === key && sortConfig.direction === "asc") {
            direction = "desc";
        }

        const sortedOrders = [...filteredOrders].sort((a, b) => {
            if (key === "createdAt") {
                const dateA = new Date(a[key]);
                const dateB = new Date(b[key]);
                return direction === "asc" ? dateA.getTime() - dateB.getTime() : dateB.getTime() - dateA.getTime();
            }
            if (!a[key] && !b[key]) return 0;
            if (!a[key]) return direction === "asc" ? 1 : -1;
            if (!b[key]) return direction === "asc" ? -1 : 1;
            if (a[key]! < b[key]!) return direction === "asc" ? -1 : 1;
            if (a[key]! > b[key]!) return direction === "asc" ? 1 : -1;
            return 0;
        });

        setFilteredOrders(sortedOrders);
        setSortConfig({ key, direction });
    };

    // Pagination logic
    const paidOrders = filteredOrders.filter((order) => order.paid);
    const indexOfLastOrder = currentPage * ordersPerPage;
    const indexOfFirstOrder = indexOfLastOrder - ordersPerPage;
    const currentOrders = paidOrders.slice(indexOfFirstOrder, indexOfLastOrder);
    const totalPages = Math.ceil(paidOrders.length / ordersPerPage);

    if (loading) return <LoadingSpinner message={translationsAccount[language].loading} />;
    if (error) return <ErrorMessage message={translationsAccount[language].error.replace("{error}", error || "Unknown error")} />;

    return (
        <div className="container mx-auto p-6">
            <h1 className="text-3xl font-bold mb-6 text-gray-800">{translationsAccount[language].heading}</h1>
            <SearchBar
                searchCode={searchCode}
                setSearchCode={setSearchCode}
                searchDateTime={searchDateTime}
                setSearchDateTime={setSearchDateTime}
                translations={translationsAccount[language]}
            />
            <OrderTable
                orders={currentOrders}
                sortConfig={sortConfig}
                handleSort={handleSort}
                translations={translationsAccount[language]}
            />
            <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={setCurrentPage}
            />
        </div>
    );
};

export default OrderHistory;