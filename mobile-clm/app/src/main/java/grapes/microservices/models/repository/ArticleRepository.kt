package grapes.microservices.models.repository

import android.util.Log
import grapes.microservices.models.data.Article
import grapes.microservices.models.network.ArticleApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArticleRepository(
    private val api: ArticleApiService
) {

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