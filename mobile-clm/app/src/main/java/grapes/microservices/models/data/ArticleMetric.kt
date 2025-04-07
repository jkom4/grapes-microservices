package grapes.microservices.models.data

abstract class ArticleMetric(
    open var price: Double,
    open var stock: Double
)

class ArticleMetricByKg(
    override var price: Double,
    override var stock: Double
) : ArticleMetric(price, stock) {
    override fun toString(): String {
        return "$price € / kg"
    }
}

class ArticleMetricByUnit(
    override var price: Double,
    override var stock: Double
) : ArticleMetric(price, stock) {
    override fun toString(): String {
        return "$price € / pc"
    }
}
