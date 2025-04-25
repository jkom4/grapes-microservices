import { toast } from "react-toastify";

export class ErrorUtils {
    static handleErrors(err: any, setError: (msg: string) => void) {
        if (typeof err === 'object' && err !== null) {
            const formattedErrors = Object.entries(err)
                .map(([key, message]) => {
                    if (typeof message === 'string') {
                        toast.error(message, { autoClose: 2000 });
                    } else {
                        toast.error("An unknown error occurred", { autoClose: 2000 });
                    }
                    return '';
                })
                .join('\n');
            if (formattedErrors) {
                setError(formattedErrors);
            }
        } else if (typeof err === 'string') {
            setError(err);
            toast.error(err, { autoClose: 2000 });
        }
        else {
            const message = err?.message || "An unexpected error has occurred. Please verify the form";
            setError(message);
            toast.error(message, { autoClose: 2000 });
        }
    }
}
