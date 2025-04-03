package grapes.microservices.models.data

data class ArticleFilterSettings(
    val query: String = "",
    val category: String = "",
    val family: String = "",
    val articleMinMaxRatting: ArticleMinMaxRatting = ArticleMinMaxRatting(),
    val articleMinMaxPrice: ArticleMinMaxPrice = ArticleMinMaxPrice()
)