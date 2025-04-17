// src/components/OrderHistory.tsx
import { useEffect, useState } from "react";
import { Order, SortConfig } from "../../utils/models/interface/Order";
import accountService from "../../services/accountService";
import { useLanguage } from "../../features/LanguageContext";
import {enUS, fr} from "date-fns/locale";
import { format } from "date-fns";


// OrderHistory Component: Displays a table of user orders with sorting, filtering, and PDF invoice viewing
const OrderHistory: React.FC = () => {
    const { language } = useLanguage(); // State to manage language toggle
    const [orders, setOrders] = useState<Order[]>([]);
    const [filteredOrders, setFilteredOrders] = useState<Order[]>([]);
    const [sortConfig, setSortConfig] = useState<SortConfig>({ key: 'createdAt', direction: 'desc' });
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [viewError, setViewError] = useState<string | null>(null);
    const [searchCode, setSearchCode] = useState<string>(''); // Search term for order code
    const [searchDateTime, setSearchDateTime] = useState<string>(''); // Search term for date and time
    const [selectedPdfUrl, setSelectedPdfUrl] = useState<string | null>(null);
    const [numPages, setNumPages] = useState<number | null>(null);

    // Text content in both languages (English and French)
    const text = {
        en: {
            heading: "Order History",
            loading: "Loading...",
            error: "Error: {error}",
            viewError: "{error}",
            orderCode: "Order Code",
            date: "Date",
            total: "Total",
            status: "Status",
            invoice: "Invoice",
            paid: "Paid",
            pending: "Pending",
            view: "View",
            close: "Close",
            na: "N/A",
            currency: "$",
            searchCodePlaceholder: "Search by order code",
            searchDateTimePlaceholder: "Search by date or time (e.g., April 17, 2025 or 09:40)",
            pdfPreview: "Invoice Preview"
        },
        fr: {
            heading: "Historique des commandes",
            loading: "Chargement...",
            error: "Erreur : {error}",
            viewError: "{error}",
            orderCode: "Code de commande",
            date: "Date",
            total: "Total",
            status: "Statut",
            invoice: "Facture",
            paid: "Payé",
            pending: "En attente",
            view: "Voir",
            close: "Fermer",
            na: "N/A",
            currency: "€",
            searchCodePlaceholder: "Rechercher par code de commande",
            searchDateTimePlaceholder: "Rechercher par date ou heure (ex. : 17 avril 2025 ou 09:40)",
            pdfPreview: "Aperçu de la facture"
        }
    };

    // Fetch orders using accountService
    useEffect(() => {
        const fetchOrders = async () => {
            try {
                const userId = 1; // Replace with logic to get user ID
                const data = await accountService.fetchOrderHistory(userId);
                setOrders(data);
                setFilteredOrders(data);
                setLoading(false);
            } catch (err) {
                setError(err instanceof Error ? err.message : text[language].error.replace('{error}', 'An error occurred'));
                setLoading(false);
            }
        };
        fetchOrders();
    }, [language]);

    // Filter orders based on search criteria
    useEffect(() => {
        const locale = language === 'en' ? enUS : fr;
        const filtered = orders.filter((order) => {
            // Search by code
            const codeMatch = searchCode
                ? order.code.toString().toLowerCase().includes(searchCode.toLowerCase())
                : true;

            // Search by date and time
            let dateTimeMatch = true;
            if (searchDateTime) {
                const orderDate = new Date(order.createdAt);
                // Format order date in multiple ways for flexible matching
                const formattedDateFull = format(orderDate, 'PPp', { locale }); // e.g., "April 17, 2025, 9:40 AM"
                const formattedDateShort = format(orderDate, 'PP', { locale }); // e.g., "April 17, 2025"
                const formattedYear = format(orderDate, 'yyyy', { locale }); // e.g., "2025"
                const formattedMonthYear = format(orderDate, 'MMMM yyyy', { locale }); // e.g., "April 2025"
                const formattedDayMonth = format(orderDate, 'd MMMM', { locale }); // e.g., "17 April"
                const formattedTime = format(orderDate, 'HH:mm', { locale }); // e.g., "09:40"

                // Check if search term matches any formatted date/time
                dateTimeMatch = [
                    formattedDateFull,
                    formattedDateShort,
                    formattedYear,
                    formattedMonthYear,
                    formattedDayMonth,
                    formattedTime
                ].some((formatted) => formatted.toLowerCase().includes(searchDateTime.toLowerCase()));
            }

            return codeMatch && dateTimeMatch;
        });
        setFilteredOrders(filtered);
    }, [orders, searchCode, searchDateTime, language]);

    // Sorting logic for the orders table
    const handleSort = (key: keyof Order) => {
        let direction: 'asc' | 'desc' = 'asc';
        if (sortConfig.key === key && sortConfig.direction === 'asc') {
            direction = 'desc';
        }

        const sortedOrders = [...filteredOrders].sort((a, b) => {
            if (key === 'createdAt') {
                const dateA = new Date(a[key]);
                const dateB = new Date(b[key]);
                return direction === 'asc' ? dateA.getTime() - dateB.getTime() : dateB.getTime() - dateA.getTime();
            }
            if (!a[key] && !b[key]) return 0;
            if (!a[key]) return direction === 'asc' ? 1 : -1;
            if (!b[key]) return direction === 'asc' ? -1 : 1;
            if (a[key]! < b[key]!) return direction === 'asc' ? -1 : 1;
            if (a[key]! > b[key]!) return direction === 'asc' ? 1 : -1;
            return 0;
        });

        setFilteredOrders(sortedOrders);
        setSortConfig({ key, direction });
    };


    // Handle successful PDF load
    const onDocumentLoadSuccess = ({ numPages }: { numPages: number }) => {
        setNumPages(numPages);
    };

    // Format date for display based on language
    const formatDate = (dateString: string): string => {
        return new Date(dateString).toLocaleString(language === 'en' ? 'en-US' : 'fr-FR', {
            dateStyle: 'medium',
            timeStyle: 'short',
        });
    };

    if (loading) {
        return <div className="text-center p-6 text-gray-600">{text[language].loading}</div>;
    }

    if (error) {
        return <div className="text-center p-6 text-red-600">{text[language].error.replace('{error}', error || 'Unknown error')}</div>;
    }

    return (
        <div className="container mx-auto p-6">
            {/* Page heading */}
            <h1 className="text-3xl font-bold mb-6 text-gray-800">{text[language].heading}</h1>
            {/* Search inputs */}
            <div className="mb-6 flex flex-col sm:flex-row gap-4">
                <input
                    type="text"
                    value={searchCode}
                    onChange={(e) => setSearchCode(e.target.value)}
                    placeholder={text[language].searchCodePlaceholder}
                    className="px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
                <input
                    type="text"
                    value={searchDateTime}
                    onChange={(e) => setSearchDateTime(e.target.value)}
                    placeholder={text[language].searchDateTimePlaceholder}
                    className="px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
            </div>
            {/* Display view error if any */}
            {viewError && (
                <div className="mb-4 p-4 bg-red-100 text-red-800 rounded-md">
                    {text[language].viewError.replace('{error}', viewError)}
                </div>
            )}
            {/* PDF viewer section */}
            {selectedPdfUrl && (
                <div className="mb-6 p-4 bg-gray-100 rounded-md">
                    <h2 className="text-xl font-semibold mb-4">{text[language].pdfPreview}</h2>
                    <button
                        onClick={() => setSelectedPdfUrl(null)}
                        className="mb-4 bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700"
                    >
                        {text[language].close}
                    </button>
                </div>
            )}
            <div className="overflow-x-auto shadow-md rounded-lg">
                <table className="min-w-full bg-white">
                    <thead className="bg-gray-100">
                    <tr>
                        <th
                            className="px-6 py-3 text-left text-sm font-semibold text-gray-600 cursor-pointer"
                            onClick={() => handleSort('code')}
                        >
                            {text[language].orderCode} {sortConfig.key === 'code' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th
                            className="px-6 py-3 text-left text-sm font-semibold text-gray-600 cursor-pointer"
                            onClick={() => handleSort('createdAt')}
                        >
                            {text[language].date} {sortConfig.key === 'createdAt' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th
                            className="px-6 py-3 text-left text-sm font-semibold text-gray-600 cursor-pointer"
                            onClick={() => handleSort('totalPrice')}
                        >
                            {text[language].total} {sortConfig.key === 'totalPrice' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-gray-600">{text[language].status}</th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-gray-600">{text[language].invoice}</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredOrders.map((order) => (
                        <tr key={order.id} className="border-b hover:bg-gray-50">
                            <td className="px-6 py-4 text-gray-700">{order.code}</td>
                            <td className="px-6 py-4 text-gray-700">{formatDate(order.createdAt)}</td>
                            <td className="px-6 py-4 text-gray-700">
                                {order.totalPrice ? `${text[language].currency}${order.totalPrice.toFixed(2)}` : '-'}
                            </td>
                            <td className="px-6 py-4">
                                <span
                                    className={`inline-block px-3 py-1 text-sm font-semibold rounded-full ${
                                        order.paid ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                                    }`}
                                >
                                    {order.paid ? text[language].paid : text[language].pending}
                                </span>
                            </td>
                            <td className="px-6 py-4">
                                {order.facturePath ? (
                                    <button
                                        className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition"
                                    >
                                        {text[language].view}
                                    </button>
                                ) : (
                                    <span className="text-gray-500">{text[language].na}</span>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default OrderHistory;