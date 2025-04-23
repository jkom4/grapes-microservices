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