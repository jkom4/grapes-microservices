package grapes.microservices.models.api

import grapes.microservices.models.data.Article
import retrofit2.http.GET

interface GrapesApi {
    @GET("clm/articles")
    suspend fun getArticles(): List<Article>
}
