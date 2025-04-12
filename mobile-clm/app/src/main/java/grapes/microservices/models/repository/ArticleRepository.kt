package grapes.microservices.models.repository

import android.util.Log
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.Category
import grapes.microservices.models.data.Family
import grapes.microservices.models.network.ArticleApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArticleRepository(
    private val api: ArticleApiService
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
            "14" to Category("14", "Nuts", "Botanical fruits considered nuts, like walnuts."),
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

    suspend fun getArticles(): List<Article> {
        return withContext(Dispatchers.IO) {
            try {
                val articles = api.getArticles()
                Log.d("ArticleRepository", "Successfully fetched ${articles.size} articles")
                articles
            } catch (e: Exception) {
                Log.e("ArticleRepository", "Failed to fetch articles: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun getArticleById(id: Int): Result<Article> {
        return withContext(Dispatchers.IO) {
            try {
                val article = api.getArticleById(id)
                Log.d("ArticleRepository", "Successfully fetched article with id $id: $article")
                Result.success(article)
            } catch (e: Exception) {
                Log.e("ArticleRepository", "Failed to fetch article with id $id: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}