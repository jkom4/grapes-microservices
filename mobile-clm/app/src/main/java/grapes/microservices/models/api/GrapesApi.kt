package grapes.microservices.models.api

import grapes.microservices.models.data.Article
import grapes.microservices.models.data.Category
import grapes.microservices.models.data.Family
import retrofit2.http.GET
import retrofit2.http.Query

interface GrapesApi {
    @GET("/articles")
    suspend fun getArticles(
        @Query("query") query: String?, // Utiliser les noms de paramètres attendus par l'API
        @Query("category") category: String?,
        @Query("family") family: String?,
        @Query("rating_max") rattingRangeMax: Float?, // Nommage cohérent (ex: snake_case)
        @Query("rating_min") rattingRangeMin: Float?,
        @Query("price_max") priceRangeMax: Float?,
        @Query("price_min") priceRangeMin: Float?
    ) : List<Article>

    @GET("/articles/minmaxcost")
    suspend fun getMinMaxCost(): ClosedFloatingPointRange<Float>

    @GET("/families")
    suspend fun getFamilies(): List<Family>

    @GET("/categories")
    suspend fun getCategories(): List<Category>
}

