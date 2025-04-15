import {PaginationControlsProps} from "../../utils/models/interface/PaginationControlsProps";

export const PaginationControls: React.FC<PaginationControlsProps> =
    ({
         currentPage,
         totalPages,
         onPrevious,
         onNext,
         translations,
     }) => (
    <div className="mt-6 flex justify-center items-center space-x-4">
        <button
            onClick={onPrevious}
            disabled={currentPage === 0}
            className="px-4 py-2 bg-accent text-white rounded-lg shadow-md hover:bg-secondary disabled:bg-gray-300 disabled:cursor-not-allowed transition-all duration-200"
        >
            {translations.pagination_previous}
        </button>
        <span className="text-sm text-gray-700">
            {translations.pagination_page.replace("{0}", (currentPage + 1).toString()).replace("{1}", totalPages.toString())}
        </span>
        <button
            onClick={onNext}
            disabled={currentPage >= totalPages - 1}
            className="px-4 py-2 bg-accent text-white rounded-lg shadow-md hover:bg-secondary disabled:bg-gray-300 disabled:cursor-not-allowed transition-all duration-200"
        >
            {translations.pagination_next}
        </button>
    </div>
);