package grapes.microservices.models.data

data class ArticleMinMaxRatting(
    var currentInterval: ClosedFloatingPointRange<Float> = 0f..5f,
    val sliderLimit: ClosedFloatingPointRange<Float> = 0f..5f,
)