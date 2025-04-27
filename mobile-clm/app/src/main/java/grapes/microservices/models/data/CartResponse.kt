package grapes.microservices.models.data

import com.google.gson.annotations.SerializedName

data class CartResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("code") val code: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("facturePath") val facturePath: String?,
    @SerializedName("totalPrice") val totalPrice: Double?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("orderItems") val orderItems: List<Any>?,
    @SerializedName("finished") val finished: Boolean,
    @SerializedName("paid") val paid: Boolean
)