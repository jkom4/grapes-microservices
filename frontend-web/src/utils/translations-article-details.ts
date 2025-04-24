export interface Translation {
    loading: string;
    origin: string;
    stock: string;
    addToCart: string;
    back: string;
    addToCartSuccess: string;
    addToCartError: string;
    kg: string;
    unit: string;
    stockError: string;
}

export const translationsArticleDetails = {
    en: {
        loading: "Loading...",
        origin: "Origin:",
        stock: "Quantity",
        addToCart: "Add to cart",
        back: "Back",
        addToCartSuccess: "Item added to cart!",
        addToCartError: "Failed to add item to cart",
        kg: "kg",
        unit: "unit",
        stockError: "Quantity exceeds available stock!",
    },
    fr: {
        loading: "Chargement...",
        origin: "Origine:",
        stock: "Quantité",
        addToCart: "Ajouter au panier",
        back: "Retour",
        addToCartSuccess: "Article ajouté au panier !",
        addToCartError: "Échec de l'ajout au panier",
        kg: "kg",
        unit: "unité",
        stockError: "La quantité dépasse le stock disponible !",
    },
};