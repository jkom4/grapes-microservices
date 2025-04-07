package grapes.microservices.models.dto

data class ArticleFilterDTO (
    val query: String,
    val category: String,
    val family: String,
    var rattingRangeMax: Float,
    var rattingRangeMin: Float,
    var priceRangeMax: Float,
    var priceRangeMin: Float
)