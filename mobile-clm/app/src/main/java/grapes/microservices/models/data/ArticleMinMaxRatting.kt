package grapes.microservices.models.data

data class ArticleMinMaxRatting(
    val sliderLimit: ClosedFloatingPointRange<Float> = 0f..5f,
    var currentInterval: ClosedFloatingPointRange<Float> = 0f..5f,
)