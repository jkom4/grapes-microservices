package grapes.microservices.models.data

data class CartItem(
    val id: Int,
    val articleId: Int,
    val articleName: String,
    val picturePath: String?,
    val quantityKg: Float,
    val quantity: Float,
    val price: Float
)

data class Cart(
    val items: List<CartItem>,
    val totalPrice: Float
)

data class InitCartRequest(val userId: Int)

data class AddToCartRequest(
    val orderId: Int,
    val articleId: Int,
    val quantityKg: Float,
    val quantity: Float
)

data class PayCartRequest(
    val orderId: Int,
    val customerName: String,
    val phoneNumber: String,
    val address: String,
    val country: String,
    val postalCode: String
)