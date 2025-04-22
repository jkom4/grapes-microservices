interface SearchBarProps {
    searchQuery: string;
    onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    placeholder: string;
}

const SearchBar: React.FC<SearchBarProps> = ({ searchQuery, onSearchChange, placeholder }) => {
    return (
        <section className="flex justify-center mb-6 px-4">
            <input
                type="text"
                placeholder={placeholder}
                value={searchQuery}
                onChange={onSearchChange}
                className="px-4 py-2 border rounded-md w-full sm:w-1/2 md:w-1/3"
            />
        </section>
    );
};

export default SearchBar;