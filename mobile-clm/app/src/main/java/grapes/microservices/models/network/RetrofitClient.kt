package grapes.microservices.models.network

import grapes.microservices.models.data.Article
import grapes.microservices.models.data.Cart
import okhttp3.OkHttpClient
import grapes.microservices.BuildConfig
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
    // Base URL for the API endpoint
    private const val BASE_URL = BuildConfig.BASE_URL

    // Create the Retrofit instance with the specified configurations
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Create an instance of the ArticleApiService to access the API endpoints
    val articleApiService: ArticleApiService by lazy {
        retrofit.create(ArticleApiService::class.java)
    }
}

// Define the API service interface with endpoints
interface ArticleApiService {
    // Fetch the list of articles from the API
    @GET("clm/articles")
    suspend fun getArticles(): List<Article>

    // Fetch an article by its ID
    @GET("/clm/articles/{id}")
    suspend fun getArticleById(@Path("id") id: Int): Article

    // Fetch the cart data using the order ID
    @GET("clm/cart/{orderId}")
    suspend fun getCart(@Path("orderId") orderId: Int): Cart

    // Remove an item from the cart using the item ID
    @DELETE("clm/cart/remove/{itemId}")
    suspend fun removeFromCart(@Path("itemId") itemId: Int): retrofit2.Response<Unit>

    // Pay for the cart using the order ID with query parameters
    @POST("clm/cart/pay/{orderId}")
    suspend fun payCart(
        @Path("orderId") orderId: Int,
        @Query("address") address: String,
        @Query("phoneNumber") phoneNumber: String,
        @Query("customerName") customerName: String,
        @Query("country") country: String,
        @Query("postalCode") postalCode: String
    ): retrofit2.Response<Unit>

    // Clear the cart using the order ID
    @DELETE("clm/cart/clear/{orderId}")
    suspend fun clearCart(@Path("orderId") orderId: Int): retrofit2.Response<Unit>

    // Initialize a new cart with a user ID
    @POST("clm/cart/init")
    suspend fun initCart(@Body request: InitCartRequest): retrofit2.Response<Unit>

    // Add an article to the cart
    @POST("clm/cart/add")
    suspend fun addToCart(@Body request: AddToCartRequest): retrofit2.Response<Unit>

    // Fetch a paginated list of articles with pagination parameters
    @GET("clm/articles")
    suspend fun getArticlesPaginated(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<Article>

    @GET("clm/articles/search")
    suspend fun searchArticles(@Query("name") name: String): List<Article>
}

// Data classes to handle request bodies for cart-related operations
data class InitCartRequest(val userId: Int)
data class AddToCartRequest(
    val orderId: Int,
    val articleId: Int,
    val quantityKg: Float,
    val quantity: Float
)