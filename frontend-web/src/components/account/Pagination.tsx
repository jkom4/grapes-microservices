interface PaginationProps {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

const Pagination: React.FC<PaginationProps> = ({ currentPage, totalPages, onPageChange }) => {
    return (
        <div className="flex justify-center mt-6">
            {currentPage > 1 && (
                <button
                    className="px-3 py-2 mx-1 bg-accent text-white rounded-md hover:bg-secondary focus:outline-none"
                    onClick={() => onPageChange(currentPage - 1)}
                >
                    {"<"}
                </button>
            )}

            {[...Array(totalPages).keys()].map((page) => {
                const pageNumber = page + 1;
                if (
                    pageNumber === 1 ||
                    pageNumber === totalPages ||
                    (pageNumber >= currentPage - 2 && pageNumber <= currentPage + 2)
                ) {
                    return (
                        <button
                            key={pageNumber}
                            onClick={() => onPageChange(pageNumber)}
                            className={`px-3 py-2 mx-1 rounded-md ${
                                pageNumber === currentPage ? "bg-accent text-white" : "bg-gray-200 hover:bg-gray-300"
                            } focus:outline-none`}
                        >
                            {pageNumber}
                        </button>
                    );
                }
                if (pageNumber === currentPage - 3 || pageNumber === currentPage + 3) {
                    return (
                        <span key={pageNumber} className="px-3 py-2 mx-1 text-gray-600">
              ...
            </span>
                    );
                }
                return null;
            })}

            {currentPage < totalPages && (
                <button
                    className="px-3 py-2 mx-1 bg-accent text-white rounded-md hover:bg-secondary focus:outline-none"
                    onClick={() => onPageChange(currentPage + 1)}
                >
                    {">"}
                </button>
            )}
        </div>
    );
};

export default Pagination;