export const ErrorDisplay: React.FC<{ message: string; onRetry: () => void }> = ({ message, onRetry }) => {
    return (
        <div className="fixed inset-0 flex items-center justify-center bg-gray-800 bg-opacity-75 z-50">
            <div className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 text-center">
                <h2 className="text-2xl font-bold text-red-600 mb-4">Authentication Error</h2>
                <p className="text-gray-700 mb-6">{message}</p>
                <button
                    onClick={onRetry}
                    className="bg-red-400 text-white px-4 py-2 rounded hover:bg-red-600 transition"
                >
                    Try Again
                </button>
            </div>
        </div>
    );
};