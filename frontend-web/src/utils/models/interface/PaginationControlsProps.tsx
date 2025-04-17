export interface PaginationControlsProps {
    currentPage: number;
    totalPages: number;
    onPrevious: () => void;
    onNext: () => void;
    translations: {
        pagination_previous: string;
        pagination_next: string;
        pagination_page: string;
    };
}