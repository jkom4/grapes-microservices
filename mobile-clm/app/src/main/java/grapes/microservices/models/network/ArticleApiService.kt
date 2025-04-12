package grapes.microservices.models.network

import grapes.microservices.models.data.Article
import retrofit2.http.GET

interface ArticleApiService {
    @GET("clm/articles")
    suspend fun getArticles(): List<Article>
}