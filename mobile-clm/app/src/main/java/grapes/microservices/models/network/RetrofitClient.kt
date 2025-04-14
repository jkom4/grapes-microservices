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
import retrofit2.http.Query

// Retrofit client to handle network operations
object RetrofitClient {
    // Base URL for the API endpoint
    private const val BASE_URL = "http://192.168.129.10:8092/"

    // Create the Retrofit instance with the specified configurations
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL) // Set the base URL for the API
        .client(OkHttpClient.Builder().build()) // Use a simple OkHttpClient without any additional configurations
        .addConverterFactory(GsonConverterFactory.create()) // Use Gson to convert JSON to Kotlin objects
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

    // Pay for the cart using the order ID
    @POST("clm/cart/pay/{orderId}")
    suspend fun payCart(@Path("orderId") orderId: Int): retrofit2.Response<Unit>

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
    suspend fun getArticlesPaginated(
        @Query("page") page: Int, // Page number
        @Query("size") size: Int // Number of articles per page
    ): List<Article>

    @GET("clm/articles/search")
    suspend fun searchArticles(@Query("name") name: String): List<Article>

}

// Data classes to handle request bodies for cart-related operations
data class InitCartRequest(val userId: Int) // Request to initialize the cart with the user ID
data class AddToCartRequest(
    val orderId: Int, // Order ID for the cart
    val articleId: Int, // Article ID to be added to the cart
    val quantityKg: Float, // Quantity in kilograms
    val quantity: Float // Quantity in units
)
