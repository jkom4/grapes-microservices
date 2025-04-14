package grapes.microservices.models.repository

import grapes.microservices.models.api.GrapesApi
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.ArticleFilter
import grapes.microservices.models.data.ArticleMetricByKg
import grapes.microservices.models.data.ArticleMetricByUnit
import grapes.microservices.models.data.Category
import grapes.microservices.models.data.Family


class ArticleRepository(
    private val grapesApi: GrapesApi
) {
    suspend fun getCategories(): List<Category> {
//        return grapesApi.getCategories()
        return listOf(
            Category("1", "Tropical", "Fruits from tropical regions like mangoes and bananas."),
            Category("2", "Citrus", "Fruits rich in Vitamin C like oranges and lemons."),
            Category("3", "Berries", "Small, juicy fruits like strawberries and blueberries."),
            Category("4", "Stone Fruits", "Fruits with a pit, such as peaches and cherries."),
            Category("5", "Temperate", "Fruits grown in temperate climates like apples and pears."),
            Category("6", "Exotic", "Rare and unique fruits like dragonfruit and lychees."),
            Category("7", "Melons", "Large, juicy fruits like watermelons and cantaloupes."),
            Category("8", "Dried Fruits", "Dehydrated fruits like raisins and dates."),
            Category("9", "Tropical Berries", "Tropical small fruits like açaí and guava."),
            Category("10", "Leafy Vegetables", "Fruits treated as vegetables like avocados."),
            Category("11", "Cactus Fruits", "Fruits from cacti like prickly pears."),
            Category("12", "Pome Fruits", "Fruits with a core, like apples and quince."),
            Category("13", "Culinary Vegetables", "Fruits used in cooking, e.g., tomatoes."),
            Category("14", "Nuts", "Botanical fruits considered nuts, like walnuts.", ),
            Category("15", "Seed Fruits", "Fruits that rely on seeds, like pomegranates.")
        )
    }

    suspend fun getFamilies(): List<Family> {
//        return grapesApi.getFamilies()
        return listOf(
            Family("1", "Rosaceae", "Family including fruits like apples, pears, and cherries."),
            Family("2", "Rutaceae", "Family including citrus fruits like oranges, lemons, and limes."),
            Family("3", "Musaceae", "Family of bananas and plantains."),
            Family("4", "Solanaceae", "Family including fruits used as vegetables like tomatoes and peppers."),
            Family("5", "Vitaceae", "Family of grapes and related fruits."),
            Family("6", "Moraceae", "Family of figs, mulberries, and breadfruit."),
            Family("7", "Cucurbitaceae", "Family of melons, pumpkins, and cucumbers."),
            Family("8", "Ericaceae", "Family of berries like blueberries and cranberries."),
            Family("9", "Lauraceae", "Family including avocados and bay laurel."),
            Family("10", "Fabaceae", "Family of legumes, sometimes including edible seeds."),
            Family("11", "Actinidiaceae", "Family including kiwifruits."),
            Family("12", "Anacardiaceae", "Family including mangoes and cashews."),
            Family("13", "Euphorbiaceae", "Family including rubber trees and cassava."),
            Family("14", "Arecaceae", "Family including palm fruits like coconuts and dates."),
            Family("15", "Malvaceae", "Family of hibiscus, okra, and cotton.")
        )
    }

    suspend fun getMinMaxCost(): ClosedFloatingPointRange<Float> {
//        return grapesApi.getMinMaxCost()
        return 0f..20f
    }

    /**
     * Return a list of articles with category and family that only contains the name
     */
    suspend fun getArticles(articleFilter: ArticleFilter = ArticleFilter()): List<Article> {
//        return grapesApi.getArticles(
//            query = articleFilter.query, // Ne pas envoyer si vide, par exemple
//            category = articleFilter.category,
//            family = articleFilter.family,
//            rattingRangeMax = articleFilter.rattingRange.currentInterval.endInclusive, // Passer directement
//            rattingRangeMin = articleFilter.rattingRange.currentInterval.start,
//            priceRangeMax = articleFilter.priceRange.currentInterval.endInclusive,
//            priceRangeMin = articleFilter.priceRange.currentInterval.start
//        )
        // TODO One transaction for all this request required to ensure that when a category is deleted during this request,
        //  there is no fetch of article with and id that references a category/family that doesn't exist anymore
        return listOf(
            Article("1", "Apple", ArticleMetricByUnit(3.5, 1.2), 5.0f, "https://r2.erweima.ai/imgcompressed/img/compressed_ef1044c4fc8d53db8f096eca109457de.webp"),
            Article("2", "Melon", ArticleMetricByKg(10.0, 10.0), 4.0f, "https://static.vecteezy.com/system/resources/previews/029/200/201/large_2x/watermelon-transparent-background-free-png.png"),
            Article("3", "Cherry", ArticleMetricByUnit(4.0, 0.8), 5.0f),
            Article("4", "Orange", ArticleMetricByUnit(2.5, 1.0), 5.0f),
            Article("5", "Pear", ArticleMetricByUnit(3.0, 1.5), 5.0f),
            Article("6", "Mango", ArticleMetricByUnit(6.0, 2.0), 5.0f),
            Article("7", "Grapes", ArticleMetricByUnit(2.0, 0.6), 5.0f),
            Article("8", "Pineapple", ArticleMetricByUnit(3.8, 1.8), 5.0f),
            Article("9", "Strawberry", ArticleMetricByUnit(5.0, 1.5), 5.0f),
            Article("10", "Watermelon", ArticleMetricByUnit(1.0, 7.0), 5.0f),
            Article("11", "Avocado", ArticleMetricByUnit(8.0, 3.0), 5.0f),
            Article("12", "Blueberry", ArticleMetricByUnit(6.5, 2.5), 5.0f),
            Article("13", "Peach", ArticleMetricByUnit(4.2, 1.7), 5.0f),
            Article("14", "Papaya", ArticleMetricByUnit(3.6, 1.4), 5.0f),
            Article("15", "Kiwi", ArticleMetricByUnit(5.3, 1.8),5.0f),
        )
    }

}