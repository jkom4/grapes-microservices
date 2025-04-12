package grapes.microservices.models.data

import com.google.gson.annotations.SerializedName

data class Article(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("family") val family: String,
    @SerializedName("description") val description: String,
    @SerializedName("metric") val metric: ArticleMetric,
    @SerializedName("rating") val rating: Float? = null,
    @SerializedName("picture") val picture: String = "https://pngimg.com/uploads/pear/pear_PNG3463.png"
)

data class ArticleMetric(
    @SerializedName("price") val price: Double,
    @SerializedName("stock") val stock: Double,
    @SerializedName("unit") val unit: String? = "unit"
) {
    fun priceToString(): String {
        return when (unit) {
            "kg" -> "$price € / kg"
            else -> "$price € / pc"
        }
    }
}