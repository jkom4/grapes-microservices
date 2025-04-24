interface LoadingSpinnerProps {
    message: string;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ message }) => {
    return <div className="text-center p-6 text-gray-600">{message}</div>;
};

export default LoadingSpinner;