import {AdminHeaderProps} from "../../utils/models/interface/AdminHeaderProps";

export const AdminHeader: React.FC<AdminHeaderProps> = ({ title, addButtonText, plusSign, onAddClick }) => (
    <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">{title}</h1>
        <button
            onClick={onAddClick}
            className="flex items-center bg-gradient-to-r from-accent to-secondary text-white px-6 py-3 rounded-lg shadow-md hover:from-pink-200 hover:to-red-300 transition-all duration-200"
        >
            <span className="mr-2">{plusSign}</span>
            {addButtonText}
        </button>
    </div>
);