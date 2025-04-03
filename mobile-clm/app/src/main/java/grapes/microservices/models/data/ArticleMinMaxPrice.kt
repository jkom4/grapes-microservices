package grapes.microservices.models.data

data class ArticleMinMaxPrice(
    var currentInterval: ClosedFloatingPointRange<Float> = 0f..100f,
    val sliderLimit: ClosedFloatingPointRange<Float> = 0f..100f,
)

