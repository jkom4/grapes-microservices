interface ToastNotificationProps {
    toast: { message: string; type: "success" | "error" } | null;
}

const ToastNotification: React.FC<ToastNotificationProps> = ({ toast }) => {
    if (!toast) return null;

    return (
        <div
            className={`fixed bottom-4 right-4 p-4 rounded-lg shadow-lg text-white ${
                toast.type === "success" ? "bg-green-500" : "bg-red-500"
            } animate-toast`}
        >
            {toast.message}
        </div>
    );
};

export default ToastNotification;