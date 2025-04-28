import { useLanguage } from "../../features/LanguageContext";
import { Order } from "../../utils/models/interface/Order";
import { translationsAccount } from "../../utils/translations-account";
import {getArticlesAPI} from "../../services/httpCommon";

interface OrderRowProps {
    order: Order;
    translations: typeof translationsAccount["en"];
}

const OrderRow: React.FC<OrderRowProps> = ({ order, translations }) => {
    const { language } = useLanguage();

    // Function to format the date for display
    const formatDate = (dateString: string): string => {
        return new Date(dateString).toLocaleString(language === "en" ? "en-US" : "fr-FR", {
            dateStyle: "medium",
            timeStyle: "short",
        });
    };

    // Function to handle the invoice download
    const handleInvoiceDownload = async (orderId: number) => {
        const url = `${getArticlesAPI.baseURL}/clm/cart/${orderId}/invoice`; // URL for the invoice

        try {
            // Make a GET request to fetch the invoice
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'accept': 'application/pdf', // Expecting a PDF file
                },
            });

            // If the response is not successful, throw an error
            if (!response.ok) {
                throw new Error('Failed to download invoice');
            }

            const blob = await response.blob(); // Convert the response to a Blob (PDF)
            const blobUrl = URL.createObjectURL(blob); // Create a temporary URL for the Blob

            const a = document.createElement("a");
            a.href = blobUrl; // Set the URL to the Blob
            a.target = "_blank"; // Open the link in a new tab
            a.download = `invoice_${orderId}.pdf`; // Set the filename for the download
            document.body.appendChild(a);
            a.click(); // Simulate a click to start the download
            document.body.removeChild(a); // Remove the temporary link from the DOM

            URL.revokeObjectURL(blobUrl); // Release the Blob URL once done
        } catch (error) {
            console.error('Download error:', error); // Log any errors
        }
    };

    return (
        <tr className="border-b hover:bg-gray-50">
            <td className="px-6 py-4 text-gray-700">{order.id}</td>
            <td className="px-6 py-4 text-gray-700">{order.code}</td>
            <td className="px-6 py-4 text-gray-700">{formatDate(order.createdAt)}</td>
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
                {/* Check if invoice is available, show a button to download */}
                {order.facturePath ? (
                    <button
                        onClick={() => handleInvoiceDownload(order.id)} // Trigger download on click
                        className="bg-accent text-white px-4 py-2 rounded-md hover:bg-accent transition inline-block"
                    >
                        {translations.view} {/* Button text */}
                    </button>
                ) : (
                    <span className="text-gray-500">{translations.na}</span> // If no invoice, show "N/A"
                )}
            </td>
        </tr>
    );
};

export default OrderRow;
