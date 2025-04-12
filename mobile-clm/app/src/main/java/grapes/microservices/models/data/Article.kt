package grapes.microservices.models.data

import com.google.gson.annotations.SerializedName

data class Article(
    @SerializedName("id") val id: Int,
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("familyId") val familyId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("priceKg") val priceKg: Double,
    @SerializedName("priceUnit") val priceUnit: Double,
    @SerializedName("stockKg") val stockKg: Int,
    @SerializedName("stockUnit") val stockUnit: Int,
    @SerializedName("origin") val origin: String,
    @SerializedName("picturePath") val picture: String = "https://pngimg.com/uploads/pear/pear_PNG3463.png"
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