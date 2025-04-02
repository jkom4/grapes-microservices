package grapes.microservices.models.repository

import grapes.microservices.models.api.GrapesApi
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.ArticleFilterSettings
import grapes.microservices.models.data.ArticleMetricByUnit
import grapes.microservices.models.data.ArticleMinMaxPrice
import grapes.microservices.models.data.Category
import grapes.microservices.models.data.Family


class ArticleRepository(
    private val grapesApi: GrapesApi
) {
    fun getCategories(): Map<String, Category> {
        return mapOf(
            "1" to Category("1", "Tropical", "Fruits from tropical regions like mangoes and bananas."),
            "2" to Category("2", "Citrus", "Fruits rich in Vitamin C like oranges and lemons."),
            "3" to Category("3", "Berries", "Small, juicy fruits like strawberries and blueberries."),
            "4" to Category("4", "Stone Fruits", "Fruits with a pit, such as peaches and cherries."),
            "5" to Category("5", "Temperate", "Fruits grown in temperate climates like apples and pears."),
            "6" to Category("6", "Exotic", "Rare and unique fruits like dragonfruit and lychees."),
            "7" to Category("7", "Melons", "Large, juicy fruits like watermelons and cantaloupes."),
            "8" to Category("8", "Dried Fruits", "Dehydrated fruits like raisins and dates."),
            "9" to Category("9", "Tropical Berries", "Tropical small fruits like açaí and guava."),
            "10" to Category("10", "Leafy Vegetables", "Fruits treated as vegetables like avocados."),
            "11" to Category("11", "Cactus Fruits", "Fruits from cacti like prickly pears."),
            "12" to Category("12", "Pome Fruits", "Fruits with a core, like apples and quince."),
            "13" to Category("13", "Culinary Vegetables", "Fruits used in cooking, e.g., tomatoes."),
            "14" to Category("14", "Nuts", "Botanical fruits considered nuts, like walnuts.", ),
            "15" to Category("15", "Seed Fruits", "Fruits that rely on seeds, like pomegranates.")
        )
    }

    fun getFamilies(): Map<String, Family> {
        return mapOf(
            "1" to Family("1", "Rosaceae", "Family including fruits like apples, pears, and cherries."),
            "2" to Family("2", "Rutaceae", "Family including citrus fruits like oranges, lemons, and limes."),
            "3" to Family("3", "Musaceae", "Family of bananas and plantains."),
            "4" to Family("4", "Solanaceae", "Family including fruits used as vegetables like tomatoes and peppers."),
            "5" to Family("5", "Vitaceae", "Family of grapes and related fruits."),
            "6" to Family("6", "Moraceae", "Family of figs, mulberries, and breadfruit."),
            "7" to Family("7", "Cucurbitaceae", "Family of melons, pumpkins, and cucumbers."),
            "8" to Family("8", "Ericaceae", "Family of berries like blueberries and cranberries."),
            "9" to Family("9", "Lauraceae", "Family including avocados and bay laurel."),
            "10" to Family("10", "Fabaceae", "Family of legumes, sometimes including edible seeds."),
            "11" to Family("11", "Actinidiaceae", "Family including kiwifruits."),
            "12" to Family("12", "Anacardiaceae", "Family including mangoes and cashews."),
            "13" to Family("13", "Euphorbiaceae", "Family including rubber trees and cassava."),
            "14" to Family("14", "Arecaceae", "Family including palm fruits like coconuts and dates."),
            "15" to Family("15", "Malvaceae", "Family of hibiscus, okra, and cotton.")
        )
    }

    fun getMinMaxCost(): ClosedFloatingPointRange<Float> {
        return 0f..20f
    }

    /**
     * Return a list of articles with category and family that only contains the name
     */
    fun getArticles(articleFilterSettings: ArticleFilterSettings = ArticleFilterSettings()): List<Article> {
        // TODO One transaction for all this request required to ensure that when a category is deleted during this request,
        //  there is no fetch of article with and id that references a category/family that doesn't exist anymore
        return listOf(
            Article("1", "Apple", "tropical", "Apple", "no description", ArticleMetricByUnit(3.5, 1.2), 5.0f, "https://r2.erweima.ai/imgcompressed/img/compressed_ef1044c4fc8d53db8f096eca109457de.webp"),
            Article("2", "Banana", "tropical", "Banana", "Rich in potassium", ArticleMetricByUnit(1.8, 0.5), 5.0f),
            Article("3", "Cherry", "temperate", "Berry", "Juicy and red", ArticleMetricByUnit(4.0, 0.8), 5.0f),
            Article("4", "Orange", "citrus", "Fruit", "Vitamin C source", ArticleMetricByUnit(2.5, 1.0), 5.0f),
            Article("5", "Pear", "temperate", "Fruit", "Sweet and juicy", ArticleMetricByUnit(3.0, 1.5), 5.0f),
            Article("6", "Mango", "tropical", "Fruit", "Exotic and sweet", ArticleMetricByUnit(6.0, 2.0), 5.0f),
            Article("7", "Grapes", "temperate", "Berry", "Delicious and healthy", ArticleMetricByUnit(2.0, 0.6), 5.0f),
            Article("8", "Pineapple", "tropical", "Fruit", "Tangy and sweet", ArticleMetricByUnit(3.8, 1.8), 5.0f),
            Article("9", "Strawberry", "temperate", "Berry", "Bright and flavorful", ArticleMetricByUnit(5.0, 1.5), 5.0f),
            Article("10", "Watermelon", "tropical", "Fruit", "Perfect for summer", ArticleMetricByUnit(1.0, 7.0), 5.0f),
            Article("11", "Avocado", "tropical", "Fruit", "Healthy and creamy", ArticleMetricByUnit(8.0, 3.0), 5.0f),
            Article("12", "Blueberry", "temperate", "Berry", "Rich in antioxidants", ArticleMetricByUnit(6.5, 2.5), 5.0f),
            Article("13", "Peach", "temperate", "Fruit", "Soft and sweet", ArticleMetricByUnit(4.2, 1.7), 5.0f),
            Article("14", "Papaya", "tropical", "Fruit", "Good for digestion", ArticleMetricByUnit(3.6, 1.4), 5.0f),
            Article("15", "Kiwi", "temperate", "Berry", "Zesty and nutritious", ArticleMetricByUnit(5.3, 1.8),5.0f),
        )
    }

}