package grapes.microservices.models.network

import android.util.Log
import com.google.gson.annotations.SerializedName
import grapes.microservices.models.data.AddToCartRequest
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.Cart
import grapes.microservices.models.data.CartResponse
import grapes.microservices.models.data.InitCartRequest
import grapes.microservices.models.data.Order
import grapes.microservices.models.data.PayCartRequest
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
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

object RetrofitClient {

    private const val BASE_URL = "http://89.168.47.217:8090/api/"

    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableListOf<Cookie>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookies.forEach { cookie ->
                cookieStore.removeAll { it.name == cookie.name }
                cookieStore.add(cookie)
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val validCookies = cookieStore.filter { cookie ->
                url.host.contains(cookie.domain ?: url.host) &&
                        url.encodedPath.startsWith(cookie.path) &&
                        !cookie.isExpired()
            }
            return validCookies
        }

        private fun Cookie.isExpired(): Boolean {
            return expiresAt < System.currentTimeMillis()
        }
    }

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            response
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val paymentApiService: PaymentApiService by lazy {
        retrofit.create(PaymentApiService::class.java)
    }

    val articleApiService: ArticleApiService by lazy {
        retrofit.create(ArticleApiService::class.java)
    }

    val orderApiService: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
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
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("redirectUrl") val redirectUrl: String,
    @SerializedName("message") val message: String? = null
) {
    val isSuccess: Boolean
        get() = success == true || status == "success"
}

// Data class for session details response
data class SessionDetailsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("amount") val amount: Float?,
    @SerializedName("merchantName") val merchantName: String?,
    @SerializedName("redirectUrl") val redirectUrl: String?,
    @SerializedName("message") val message: String?
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
    @POST("payment/login/payment-initiate")
    suspend fun initiatePayment(@Body request: PaymentInitiateRequest): Response<PaymentInitiateResponse>

    @GET("payment/session-details")
    suspend fun getSessionDetails(): Response<SessionDetailsResponse>
}