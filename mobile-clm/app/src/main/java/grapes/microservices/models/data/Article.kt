package grapes.microservices.models.data

class Article(
    var id: String,
    var name: String,
    var metric: ArticleMetric,
    var rating: Float? = null,
    var picture: String = "https://pngimg.com/uploads/pear/pear_PNG3463.png"
)