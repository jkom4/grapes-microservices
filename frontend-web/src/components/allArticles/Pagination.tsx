interface PaginationProps {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

const Pagination: React.FC<PaginationProps> = ({ currentPage, totalPages, onPageChange }) => {
    return (
        <div className="flex justify-center mt-6">
            <button
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 1}
                className="px-4 py-2 mx-2 bg-accent text-white rounded-md disabled:opacity-50"
            >
                Previous
            </button>
            <span className="px-4 py-2 mx-2 text-lg">
        {currentPage} / {totalPages}
      </span>
            <button
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="px-4 py-2 mx-2 bg-accent text-white rounded-md disabled:opacity-50"
            >
                Next
            </button>
        </div>
    );
};

export default Pagination;