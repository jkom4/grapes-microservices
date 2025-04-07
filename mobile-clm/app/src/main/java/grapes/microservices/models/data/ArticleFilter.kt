package grapes.microservices.models.data

import android.util.Log

data class ArticleFilter(
    val query: String = "",
    val category: String = "",
    val family: String = "",
    var rattingRange: ArticleMinMaxRatting = ArticleMinMaxRatting(),
    var priceRange: ArticleMinMaxPrice = ArticleMinMaxPrice()
) {
    fun sameAs(other: ArticleFilter): Boolean {
        return query == other.query &&
                category == other.category &&
                family == other.family &&
                rattingRange.currentInterval == other.rattingRange.currentInterval &&
                priceRange.currentInterval == other.priceRange.currentInterval
    }

    /**
     *  Instead of creating a new object from scratch, we keep the minimum and maximum price
     *   value among all the articles, so we don't need to fetch them again from the API.
     *  @return ArticleFilterSettings with the minimum and maximum price values kept
     */
    fun getReset(): ArticleFilter {
        return ArticleFilter(
            priceRange = ArticleMinMaxPrice(this.priceRange.sliderLimit),
        )
    }

    fun isReset(): Boolean {
        Log.d("filter equal?", "priceRange: ${priceRange.currentInterval} == ${getReset().priceRange.currentInterval}, priceLimit : ${priceRange.sliderLimit} == ${getReset().priceRange.sliderLimit}")
        return this == getReset()
    }
}
