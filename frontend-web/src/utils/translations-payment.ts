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
    payWithStripe: string;
    secureCheckoutDescription: string;
    country: string;
    city: string;
    state: string;
    zip: string;
    termsAccepted: string;
    termsLabel: string;
    paymentSuccess: string;
    thankYou: string;
    orderDetails: string;
    backToHome: string;
    errorFetchingCart: string;
    errorRemovingItem: string;
    invalidOrderId: string;
    loginRequired: string;
    payWith3DSecure: string;
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
        payWithStripe: "Pay with Stripe",
        secureCheckoutDescription:
            "Ensuring your financial and personal details are secure during every transaction.",
        country: "Country",
        city: "City",
        state: "State",
        zip: "ZIP Code",
        termsAccepted: "I accept the terms and conditions",
        termsLabel: "Accept Terms",
        paymentSuccess: "Payment Successful",
        thankYou: "Thank you for your purchase!",
        orderDetails: "Order Details",
        backToHome: "Back to Home",
        errorFetchingCart: "Failed to fetch cart",
        errorRemovingItem: "Failed to remove item from cart",
        invalidOrderId: "Invalid order ID",
        loginRequired: "You must be logged in to proceed with payment.",
        payWith3DSecure: "Pay with 3D Secure"
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
        payWithStripe: "Payer avec Stripe",
        secureCheckoutDescription:
            "Garantir la sécurité de vos informations financières et personnelles lors de chaque transaction.",
        country: "Pays",
        city: "Ville",
        state: "État",
        zip: "Code postal",
        termsAccepted: "J'accepte les conditions générales",
        termsLabel: "Accepter les conditions",
        paymentSuccess: "Paiement réussi",
        thankYou: "Merci pour votre achat !",
        orderDetails: "Détails de la commande",
        backToHome: "Retour à l'accueil",
        errorFetchingCart: "Échec de la récupération du panier",
        errorRemovingItem: "Échec de la suppression de l'article du panier",
        invalidOrderId: "ID de commande invalide",
        loginRequired: "Vous devez être connecté pour procéder au paiement.",
        payWith3DSecure: "Payer avec 3D Secure"
    },
};