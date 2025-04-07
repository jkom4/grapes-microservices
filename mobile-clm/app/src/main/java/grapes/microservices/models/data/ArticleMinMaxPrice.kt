package grapes.microservices.models.data

data class ArticleMinMaxPrice(
    val sliderLimit: ClosedFloatingPointRange<Float> = 0f..100f,
    val currentInterval: ClosedFloatingPointRange<Float> = sliderLimit,
)