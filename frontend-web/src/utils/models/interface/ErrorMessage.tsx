interface ErrorMessageProps {
    message: string;
}

const ErrorMessage: React.FC<ErrorMessageProps> = ({ message }) => {
    return <div className="text-center p-6 text-red-600">{message}</div>;
};

export default ErrorMessage;