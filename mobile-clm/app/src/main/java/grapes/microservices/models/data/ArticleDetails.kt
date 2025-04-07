package grapes.microservices.models.data

class ArticleDetails(
    var id: String,
    var name: String,
    var category: String,
    var family: String,
    var description: String,
    var metric: ArticleMetric,
    var rating: Float,
    var messages: List<Opinion>,
    var picture: String = "https://pngimg.com/uploads/pear/pear_PNG3463.png"
)