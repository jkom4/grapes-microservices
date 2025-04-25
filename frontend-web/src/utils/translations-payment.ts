export interface Translation {
    checkout: string;
    emptyCart: string;
    titleCart: string;
    subtotal: string;
    shipping: string;
    total: string;
    fullName: string;
    email: string;
    phone: string;
    address: string;
    payNow: string;
    cardNumber: string;
    cardExpiry: string;
    cardCVC: string;
    terms: string;
    completeForm: string;
    processing: string;
    paymentError: string;
    transactionSummary: string;
    shippingAddress: string;
    itemsOrdered: string;
    redirecting: string;
    secureCheckout: string;
    kg: string;
    unit: string;
}

export const translationsPayment: Record<"en" | "fr", Translation> = {
    en: {
        checkout: "Checkout",
        emptyCart: "Your cart is empty",
        titleCart: "Your Cart",
        subtotal: "Subtotal",
        shipping: "Shipping",
        total: "Total",
        fullName: "Full Name",
        email: "Email",
        phone: "Phone",
        address: "Address",
        payNow: "Pay Now",
        cardNumber: "Card Number",
        cardExpiry: "MM/YY",
        cardCVC: "CVC",
        terms: "I accept the terms and conditions",
        completeForm: "Please complete all required fields",
        processing: "Processing...",
        paymentError: "Payment failed. Please try again.",
        transactionSummary: "Transaction Successful!",
        shippingAddress: "Shipping Address",
        itemsOrdered: "Items Ordered",
        redirecting: "Redirecting to homepage in a few seconds...",
        secureCheckout: "Secure Checkout",
        kg: "kg",
        unit: "unit",
    },
    fr: {
        checkout: "Paiement",
        emptyCart: "Votre panier est vide",
        titleCart: "Votre Panier",
        subtotal: "Sous-total",
        shipping: "Frais de livraison",
        total: "Total",
        fullName: "Nom complet",
        email: "Email",
        phone: "Téléphone",
        address: "Adresse",
        payNow: "Payer maintenant",
        cardNumber: "Numéro de carte",
        cardExpiry: "MM/AA",
        cardCVC: "CVC",
        terms: "J'accepte les conditions générales",
        completeForm: "Veuillez remplir tous les champs requis",
        processing: "Traitement...",
        paymentError: "Échec du paiement. Veuillez réessayer.",
        transactionSummary: "Transaction réussie !",
        shippingAddress: "Adresse de livraison",
        itemsOrdered: "Articles commandés",
        redirecting: "Redirection vers la page d'accueil dans quelques secondes...",
        secureCheckout: "Paiement sécurisé",
        kg: "kg",
        unit: "unité",
    },
};