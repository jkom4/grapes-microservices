import React, {useEffect, useState} from "react";
import Loader from "../components/Loader";
import { toast } from "react-toastify";

type Props = {
    showModal: boolean;
    token: string;
    onClose: () => void;
    credentialType: "password" | "pinCode";
    handleUpdate: (current: string, updated: string, token: string) => Promise<Response>;
};

export default function UpdateCredentialModal({
                                                  showModal,
                                                  token,
                                                  onClose,
                                                  credentialType,
                                                  handleUpdate
                                              }: Props) {
    const [current, setCurrent] = useState("");
    const [updated, setUpdated] = useState("");
    const [confirm, setConfirm] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const label = credentialType === "password" ? "Password" : "PIN Code";

    const resetForm = () => {
        setCurrent("");
        setUpdated("");
        setConfirm("");
        setError("");
    };

    useEffect(() => {
        if (showModal) {
            resetForm();
        }
    }, [showModal]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);

        if (updated !== confirm) {
            toast.error(`${label}s do not match.`);
            setLoading(false);
            return;
        }

        try {
            const response = await handleUpdate(current, updated, token);
            if (response.ok) {
                toast.success(`${label} updated successfully.`);
                resetForm();
                onClose();
            } else {
                const data = await response.json();
                setError(data.message || "An error occurred.");
            }
        } catch (err) {
            // @ts-ignore
            setError(`${err.message}`);
        } finally {
            setLoading(false);
        }
    };

    if (!showModal) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
            {loading && <Loader />}
            <div className="bg-white w-full max-w-md rounded-2xl shadow-lg p-8 relative">
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 text-gray-600 hover:text-gray-900 focus:outline-none"
                >
                    <span className="text-2xl">&times;</span>
                </button>

                <h2 className="text-2xl font-semibold mb-6 text-center">
                    Change your {label.toLowerCase()}
                </h2>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium mb-1">
                            Current {label}
                        </label>
                        <input
                            type="password"
                            value={current}
                            onChange={(e) => setCurrent(e.target.value)}
                            required
                            className="w-full border border-gray-300 rounded-xl px-4 py-2"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium mb-1">
                            New {label}
                        </label>
                        <input
                            type="password"
                            value={updated}
                            onChange={(e) => setUpdated(e.target.value)}
                            required
                            className="w-full border border-gray-300 rounded-xl px-4 py-2"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium mb-1">
                            Confirm New {label}
                        </label>
                        <input
                            type="password"
                            value={confirm}
                            onChange={(e) => setConfirm(e.target.value)}
                            required
                            className="w-full border border-gray-300 rounded-xl px-4 py-2"
                        />
                    </div>

                    {error && <p className="text-red-500 text-sm">{error}</p>}

                    <button
                        type="submit"
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 rounded-xl transition duration-200"
                    >
                        Update {label}
                    </button>
                </form>
            </div>
        </div>
    );
}
