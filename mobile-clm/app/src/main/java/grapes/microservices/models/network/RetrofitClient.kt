package grapes.microservices.models.network

import grapes.microservices.models.data.Article
import grapes.microservices.models.data.Cart
import grapes.microservices.models.data.CartResponse
import grapes.microservices.models.data.Order
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit client to handle network operations
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

    val orderApiService: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }
}

interface ArticleApiService {
    @GET("clm/articles")
    suspend fun getArticles(): List<Article>

    @GET("/clm/articles/{id}")
    suspend fun getArticleById(@Path("id") id: Int): Article

    @GET("clm/cart/{orderId}")
    suspend fun getCart(@Path("orderId") orderId: Int): Cart

    @DELETE("clm/cart/remove/{orderId}/{itemId}")
    suspend fun removeFromCart(
        @Path("orderId") orderId: Int,
        @Path("itemId") itemId: Int
    ): Response<Unit>

    @POST("clm/cart/pay")
    suspend fun payCart(@Body request: PayCartRequest): Response<Unit>

    @DELETE("clm/cart/clear/{orderId}")
    suspend fun clearCart(@Path("orderId") orderId: Int): retrofit2.Response<Unit>

    @POST("clm/cart/init")
    suspend fun initCart(@Body request: InitCartRequest): Response<CartResponse>

    @POST("clm/cart/add")
    suspend fun addToCart(@Body request: AddToCartRequest): retrofit2.Response<Unit>

    @GET("clm/articles")
    suspend fun getArticlesPaginated(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<Article>

    @GET("clm/articles/search")
    suspend fun searchArticles(@Query("name") name: String): List<Article>
}

interface OrderApiService {
    @GET("cll/orders/history/{userId}")
    suspend fun getOrderHistory(
        @Path("userId") userId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("code") code: Int? = null,
        @Query("date") date: String? = null
    ): List<Order>
}

data class InitCartRequest(val userId: Int)

data class AddToCartRequest(
    val orderId: Int,
    val articleId: Int,
    val quantityKg: Float,
    val quantity: Float
)

data class PayCartRequest(
    val orderId: Int,
    val customerName: String,
    val phoneNumber: String,
    val address: String,
    val country: String,
    val postalCode: String
)