import { useEffect, useState } from "react";
import { Order, SortConfig } from "../../utils/models/interface/Order";
import accountService from "../../services/accountService";
import { useLanguage } from "../../features/LanguageContext";
import { enUS, fr } from "date-fns/locale";
import { format } from "date-fns";

// OrderHistory Component
const OrderHistory: React.FC = () => {
    const { language } = useLanguage();
    const [orders, setOrders] = useState<Order[]>([]);
    const [filteredOrders, setFilteredOrders] = useState<Order[]>([]);
    const [sortConfig, setSortConfig] = useState<SortConfig>({ key: 'createdAt', direction: 'desc' });
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [searchCode, setSearchCode] = useState<string>('');
    const [searchDateTime, setSearchDateTime] = useState<string>('');
    const [numPages, setNumPages] = useState<number | null>(null);
    const [currentPage, setCurrentPage] = useState<number>(1);
    const ordersPerPage = 20; // Constant for number of orders per page

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
            searchDateTimePlaceholder: "Search by date or time",
            pdfPreview: "Invoice Preview",
            pagination: "Page {currentPage} of {totalPages}",
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
            searchDateTimePlaceholder: "Rechercher par date ou heure",
            pdfPreview: "Aperçu de la facture",
            pagination: "Page {currentPage} sur {totalPages}",
        }
    };

    // Fetch orders using accountService
    useEffect(() => {
        const fetchOrders = async () => {
            try {
                const userId = 1; // Replace with logic to get user ID
                const data = await accountService.fetchOrderHistory(userId);
                console.log(data);
                setOrders(data);
                setFilteredOrders(data); // Store all orders initially
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
            const codeMatch = searchCode
                ? order.code.toString().toLowerCase().includes(searchCode.toLowerCase())
                : true;

            let dateTimeMatch = true;
            if (searchDateTime) {
                const orderDate = new Date(order.createdAt);
                const formattedDateFull = format(orderDate, 'PPp', { locale });
                const formattedDateShort = format(orderDate, 'PP', { locale });
                const formattedYear = format(orderDate, 'yyyy', { locale });
                const formattedMonthYear = format(orderDate, 'MMMM yyyy', { locale });
                const formattedDayMonth = format(orderDate, 'd MMMM', { locale });
                const formattedTime = format(orderDate, 'HH:mm', { locale });

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
        setFilteredOrders(filtered); // Update the filtered orders
        setCurrentPage(1); // Reset to the first page when search changes
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

    // Pagination logic
    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    // Calculate the orders to display on the current page
    const indexOfLastOrder = currentPage * ordersPerPage;
    const indexOfFirstOrder = indexOfLastOrder - ordersPerPage;
    const currentOrders = filteredOrders.slice(indexOfFirstOrder, indexOfLastOrder);

    // Calculate total pages
    const totalPages = Math.ceil(filteredOrders.length / ordersPerPage);

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
            <h1 className="text-3xl font-bold mb-6 text-gray-800">{text[language].heading}</h1>
            <div className="mb-6 flex flex-col sm:flex-row gap-4">
                <input
                    type="text"
                    value={searchCode}
                    onChange={(e) => setSearchCode(e.target.value)}
                    placeholder={text[language].searchCodePlaceholder}
                    className="px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-accent"
                />
                <input
                    type="text"
                    value={searchDateTime}
                    onChange={(e) => setSearchDateTime(e.target.value)}
                    placeholder={text[language].searchDateTimePlaceholder}
                    className="px-6 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-accent"
                />
            </div>

            <div className="overflow-x-auto shadow-md rounded-lg">
                <table className="min-w-full bg-white">
                    <thead className="bg-gray-100">
                    <tr>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-gray-600 cursor-pointer" onClick={() => handleSort('code')}>
                            {text[language].orderCode} {sortConfig.key === 'code' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-gray-600 cursor-pointer" onClick={() => handleSort('createdAt')}>
                            {text[language].date} {sortConfig.key === 'createdAt' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-gray-600 cursor-pointer" onClick={() => handleSort('totalPrice')}>
                            {text[language].total} {sortConfig.key === 'totalPrice' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-gray-600">{text[language].status}</th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-gray-600">{text[language].invoice}</th>
                    </tr>
                    </thead>
                    <tbody>
                    {currentOrders.map((order) => (
                        <tr key={order.id} className="border-b hover:bg-gray-50">
                            <td className="px-6 py-4 text-gray-700">{order.code}</td>
                            <td className="px-6 py-4 text-gray-700">{formatDate(order.createdAt)}</td>
                            <td className="px-6 py-4 text-gray-700">
                                {order.totalPrice ? `${text[language].currency}${order.totalPrice.toFixed(2)}` : '-'}
                            </td>
                            <td className="px-6 py-4">
                  <span className={`inline-block px-3 py-1 text-sm font-semibold rounded-full ${order.paid ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                    {order.paid ? text[language].paid : text[language].pending}
                  </span>
                            </td>
                            <td className="px-6 py-4">
                                {order.facturePath ? (
                                    <button
                                        onClick={() => {
                                            const link = document.createElement('a');
                                            document.body.appendChild(link);
                                            link.click();
                                            document.body.removeChild(link);
                                        }}
                                        className="bg-accent text-white px-4 py-2 rounded-md hover:bg-accent transition"
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

            {/* Pagination Section */}
            <div className="flex justify-center mt-6">
                {/* Previous Button */}
                {currentPage > 1 && (
                    <button
                        className="px-3 py-2 mx-1 bg-accent text-white rounded-md hover:bg-secondary focus:outline-none"
                        onClick={() => handlePageChange(currentPage - 1)}
                    >
                        &lt;
                    </button>
                )}

                {/* Page Numbers */}
                {[...Array(totalPages).keys()].map((page) => {
                    const pageNumber = page + 1;
                    if (pageNumber === 1 || pageNumber === totalPages || (pageNumber >= currentPage - 2 && pageNumber <= currentPage + 2)) {
                        return (
                            <button
                                key={pageNumber}
                                onClick={() => handlePageChange(pageNumber)}
                                className={`px-3 py-2 mx-1 rounded-md ${pageNumber === currentPage ? 'bg-accent text-white' : 'bg-gray-200 hover:bg-gray-300'} focus:outline-none`}
                            >
                                {pageNumber}
                            </button>
                        );
                    }
                    if (pageNumber === currentPage - 3 || pageNumber === currentPage + 3) {
                        return <span key={pageNumber} className="px-3 py-2 mx-1 text-gray-600">...</span>;
                    }
                    return null;
                })}

                {/* Next Button */}
                {currentPage < totalPages && (
                    <button
                        className="px-3 py-2 mx-1 bg-accent text-white rounded-md hover:bg-secondary focus:outline-none"
                        onClick={() => handlePageChange(currentPage + 1)}
                    >
                        &gt;
                    </button>
                )}
            </div>
        </div>
    );
};

export default OrderHistory;
