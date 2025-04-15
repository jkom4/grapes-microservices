package grapes.microservices.models.data

class Article(
    var id: String,
    var name: String,
    var category: String,
    var family: String,
    var description: String,
    var metric: ArticleMetric,
    var rating: Float? = null,
    var picture: String = "https://pngimg.com/uploads/pear/pear_PNG3463.png"
)

abstract class ArticleMetric(
    open var price: Double,
    open var stock: Double
) {
    abstract fun priceToString(): String
}

class ArticleMetricByKg(
    override var price: Double,
    override var stock: Double
) : ArticleMetric(price, stock) {
    override fun priceToString(): String {
        return "$price € / kg"
    }
}

class ArticleMetricByUnit(
    override var price: Double,
    override var stock: Double
) : ArticleMetric(price, stock) {
    override fun priceToString(): String {
        return "$price € / pc"
    }
}
