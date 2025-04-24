import { translationsAccount } from "../../utils/translations-account";

interface SearchBarProps {
    searchCode: string;
    setSearchCode: (value: string) => void;
    searchDateTime: string;
    setSearchDateTime: (value: string) => void;
    translations: typeof translationsAccount["en"];
}

const SearchBar: React.FC<SearchBarProps> = ({
                                                 searchCode,
                                                 setSearchCode,
                                                 searchDateTime,
                                                 setSearchDateTime,
                                                 translations,
                                             }) => {
    return (
        <div className="mb-6 flex flex-col sm:flex-row gap-4">
            <input
                type="text"
                value={searchCode}
                onChange={(e) => setSearchCode(e.target.value)}
                placeholder={translations.searchCodePlaceholder}
                className="px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-accent"
            />
            <input
                type="text"
                value={searchDateTime}
                onChange={(e) => setSearchDateTime(e.target.value)}
                placeholder={translations.searchDateTimePlaceholder}
                className="px-6 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-accent"
            />
        </div>
    );
};

export default SearchBar;