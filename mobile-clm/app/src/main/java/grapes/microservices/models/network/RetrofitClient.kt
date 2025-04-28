package grapes.microservices.models.network

import com.google.gson.annotations.SerializedName
import grapes.microservices.models.data.AddToCartRequest
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.Cart
import grapes.microservices.models.data.CartResponse
import grapes.microservices.models.data.InitCartRequest
import grapes.microservices.models.data.Order
import grapes.microservices.models.data.PayCartRequest
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit client to handle network operations
object RetrofitClient {
    private const val BASE_URL = "http://89.168.47.217:8090/api/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val articleApiService: ArticleApiService by lazy {
        retrofit.create(ArticleApiService::class.java)
    }

    val orderApiService: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }

    val paymentApiService: PaymentApiService by lazy {
        retrofit.create(PaymentApiService::class.java)
    }
}

// Data class for payment initiation request
data class PaymentInitiateRequest(
    @SerializedName("amount") val amount: Float,
    @SerializedName("merchantId") val merchantId: String,
    @SerializedName("redirectUrl") val redirectUrl: String
)

// Data class for payment initiation response
data class PaymentInitiateResponse(
    @SerializedName("redirectUrl") val redirectUrl: String
)

interface ArticleApiService {
    @GET("clm/articles")
    suspend fun getArticles(): List<Article>

    @GET("clm/articles/{id}")
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
    suspend fun clearCart(@Path("orderId") orderId: Int): Response<Unit>

    @POST("clm/cart/init")
    suspend fun initCart(@Body request: InitCartRequest): Response<CartResponse>

    @POST("clm/cart/add")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<Unit>

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

    @GET("cll/deliveries/status/{orderId}")
    suspend fun getDeliveryStatus(@Path("orderId") orderId: Int): String
}

interface PaymentApiService {
    // Initiates payment and returns a redirect URL
    @POST("payment/login/payment-initiate")
    suspend fun initiatePayment(@Body request: PaymentInitiateRequest): Response<PaymentInitiateResponse>
}