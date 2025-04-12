package grapes.microservices.models.network

import grapes.microservices.models.data.Article
import grapes.microservices.models.data.Cart
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

object RetrofitClient {
    private const val BASE_URL = "http://192.168.129.10:8092/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val articleApiService: ArticleApiService by lazy {
        retrofit.create(ArticleApiService::class.java)
    }
}

interface ArticleApiService {
    @GET("clm/articles")
    suspend fun getArticles(): List<Article>

    @GET("clm/articles/{id}")
    suspend fun getArticleById(@Path("id") id: Int): Article

    @GET("clm/cart/{orderId}")
    suspend fun getCart(@Path("orderId") orderId: Int): Cart

    @DELETE("clm/cart/remove/{itemId}")
    suspend fun removeFromCart(@Path("itemId") itemId: Int): retrofit2.Response<Unit>

    @POST("clm/cart/pay/{orderId}")
    suspend fun payCart(@Path("orderId") orderId: Int): retrofit2.Response<Unit>

    @DELETE("clm/cart/clear/{orderId}")
    suspend fun clearCart(@Path("orderId") orderId: Int): retrofit2.Response<Unit>

    @POST("clm/cart/init")
    suspend fun initCart(@Body request: InitCartRequest): retrofit2.Response<Unit>

    @POST("clm/cart/add")
    suspend fun addToCart(@Body request: AddToCartRequest): retrofit2.Response<Unit>
}

data class InitCartRequest(val userId: Int)
data class AddToCartRequest(
    val orderId: Int,
    val articleId: Int,
    val quantityKg: Float,
    val quantity: Float
)