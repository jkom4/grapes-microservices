package grapes.microservices.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import grapes.microservices.models.data.AddToCartRequest
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.Cart
import grapes.microservices.models.data.PayCartRequest
import grapes.microservices.models.network.ArticleApiService
import grapes.microservices.models.network.PaymentApiService
import grapes.microservices.models.network.PaymentInitiateRequest
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.models.utils.CartManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder

sealed class ArticleState {
    object Loading : ArticleState()
    data class Success(val article: Article) : ArticleState()
    data class Error(val message: String) : ArticleState()
}

sealed class CartState {
    object Idle : CartState()
    object Loading : CartState()
    object Success : CartState()
    data class Error(val message: String) : CartState()
}

sealed class ArticlePaginationState {
    object Loading : ArticlePaginationState()
    data class Success(val articles: List<Article>, val currentPage: Int) : ArticlePaginationState()
    data class Error(val message: String) : ArticlePaginationState()
}

sealed class CartScreenState {
    object Loading : CartScreenState()
    data class Success(val cart: Cart) : CartScreenState()
    data class Error(val message: String) : CartScreenState()
}

sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    object Success : PaymentState()
    data class Error(val message: String) : PaymentState()
}

class ArticleViewModel(
    private val repository: ArticleRepository,
    private val apiService: ArticleApiService,
    private val paymentApiService: PaymentApiService,
    private val cartManager: CartManager
) : ViewModel() {
    private companion object {
        const val DEFAULT_USER_ID = 1
        const val PAGE_SIZE = 20
    }

    private val _articleState = MutableStateFlow<ArticleState>(ArticleState.Loading)
    val articleState: StateFlow<ArticleState> = _articleState

    private val _cartState = MutableStateFlow<CartState>(CartState.Idle)
    val cartState: StateFlow<CartState> = _cartState

    private val _cartScreenState = MutableStateFlow<CartScreenState>(CartScreenState.Loading)
    val cartScreenState: StateFlow<CartScreenState> = _cartScreenState

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    private val _articlePaginationState = MutableStateFlow<ArticlePaginationState>(ArticlePaginationState.Loading)
    val articlePaginationState: StateFlow<ArticlePaginationState> = _articlePaginationState

    private var currentPage = 0

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    init {
        loadArticles()
    }

    fun fetchArticleById(id: Int) {
        viewModelScope.launch {
            _articleState.value = ArticleState.Loading
            val result = repository.getArticleById(id)
            _articleState.value = when {
                result.isSuccess -> ArticleState.Success(result.getOrNull()!!)
                result.isFailure -> ArticleState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                else -> ArticleState.Error("Unexpected result state")
            }
        }
    }

    fun loadArticles() {
        viewModelScope.launch {
            _articlePaginationState.value = ArticlePaginationState.Loading
            try {
                val articles = repository.getArticles()
                if (articles.isNotEmpty()) {
                    _articlePaginationState.value = ArticlePaginationState.Success(articles, currentPage)
                } else {
                    _articlePaginationState.value = ArticlePaginationState.Error("No more articles available.")
                }
            } catch (e: Exception) {
                _articlePaginationState.value = ArticlePaginationState.Error("Error loading articles: ${e.message}")
            }
        }
    }

    fun addToCart(articleId: Int, quantityKg: Float, quantityUnit: Int) {
        viewModelScope.launch {
            _cartState.value = CartState.Loading
            try {
                val orderId = cartManager.orderId.value
                if (orderId == null) {
                    _cartState.value = CartState.Error("Cart not initialized")
                    return@launch
                }

                val addResponse = apiService.addToCart(
                    AddToCartRequest(
                        orderId = orderId,
                        articleId = articleId,
                        quantityKg = quantityKg,
                        quantity = quantityUnit.toFloat()
                    )
                )
                if (addResponse.isSuccessful) {
                    _cartState.value = CartState.Success
                    fetchCart() // Refresh cart after an item added
                } else {
                    _cartState.value = CartState.Error("This product is out of stock")
                }
            } catch (e: Exception) {
                _cartState.value = CartState.Error(e.message ?: "Network error")
            }
        }
    }

    fun fetchCart() {
        viewModelScope.launch {
            val orderId = cartManager.orderId.value
            if (orderId == null) {
                _cartScreenState.value = CartScreenState.Error("Cart not initialized")
                Log.e(TAG, "fetchCart failed: orderId is null")
                return@launch
            }
            _cartScreenState.value = CartScreenState.Loading
            try {
                val cart = apiService.getCart(orderId)
                Log.d(TAG, "Cart fetched: $cart")
                cart.items.forEach { item ->
                    Log.d(TAG, "Item: ${item.articleName}, quantityKg: ${item.quantityKg}, quantity: ${item.quantity}, price: ${item.price}, itemTotal: ${if (item.quantityKg > 0) item.quantityKg * item.price else item.quantity * item.price}")
                }
                Log.d(TAG, "API totalPrice: ${cart.totalPrice}")
                // Calculate totalPrice
                val calculatedTotalPrice = cart.items.sumOf { item ->
                    if (item.quantityKg > 0) {
                        (item.quantityKg * item.price).toDouble()
                    } else {
                        (item.quantity * item.price).toDouble()
                    }
                }.toFloat()
                Log.d(TAG, "Calculated totalPrice: $calculatedTotalPrice")
                // Create a new object Cart with the totalPrice
                val correctedCart = cart.copy(totalPrice = calculatedTotalPrice)
                _cartScreenState.value = CartScreenState.Success(correctedCart)
            } catch (e: Exception) {
                _cartScreenState.value = CartScreenState.Error(e.message ?: "Error fetching cart")
                Log.e(TAG, "fetchCart error: ${e.message}")
            }
        }
    }

    fun removeFromCart(itemId: Int) {
        viewModelScope.launch {
            val orderId = cartManager.orderId.value
            Log.d(TAG, "removeFromCart with orderId: $orderId, itemId: $itemId")
            if (orderId == null) {
                _cartScreenState.value = CartScreenState.Error("Cart not initialized")
                Log.e(TAG, "removeFromCart failed: orderId is null")
                return@launch
            }
            try {
                val response = apiService.removeFromCart(orderId, itemId)
                if (response.isSuccessful) {
                    fetchCart()
                    Log.d(TAG, "removeFromCart successful for orderId: $orderId, itemId: $itemId")
                } else {
                    _cartScreenState.value = CartScreenState.Error("Error removing item from cart: HTTP ${response.code()}")
                    Log.e(TAG, "removeFromCart failed: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                _cartScreenState.value = CartScreenState.Error(e.message ?: "Network error")
                Log.e(TAG, "removeFromCart error: ${e.message}")
            }
        }
    }

    suspend fun payAndClearCart(
        address: String,
        phoneNumber: String,
        customerName: String,
        country: String,
        postalCode: String
    ): Result<Unit> {
        val orderId = cartManager.orderId.value
        Log.d(TAG, "payAndClearCart with orderId: $orderId")
        if (orderId == null) {
            _paymentState.value = PaymentState.Error("Cart not initialized")
            Log.e(TAG, "payAndClearCart failed: orderId is null")
            return Result.failure(Exception("Cart not initialized"))
        }
        val payRequest = PayCartRequest(
            orderId = orderId,
            customerName = customerName,
            phoneNumber = phoneNumber,
            address = address,
            country = country,
            postalCode = postalCode
        )
        Log.d(TAG, "PayCartRequest: $payRequest")
        _paymentState.value = PaymentState.Loading
        return try {
            val payResponse = apiService.payCart(payRequest)
            if (payResponse.isSuccessful) {
                cartManager.clearCart()
                _paymentState.value = PaymentState.Success
                Log.d(TAG, "payAndClearCart successful for orderId: $orderId")
                Result.success(Unit)
            } else {
                val errorMessage = "Payment error: HTTP ${payResponse.code()}"
                _paymentState.value = PaymentState.Error(errorMessage)
                Log.e(TAG, "payAndClearCart failed: HTTP ${payResponse.code()}, Response: ${payResponse.errorBody()?.string()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            _paymentState.value = PaymentState.Error(e.message ?: "Network error")
            Log.e(TAG, "payAndClearCart error: ${e.message}")
            Result.failure(e)
        }
    }

    fun initiatePayment(totalAmount: Float, redirectUrl: String, onRedirect: (String) -> Unit) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            try {
                val request = PaymentInitiateRequest(
                    amount = totalAmount,
                    merchantId = "grapes",
                    redirectUrl = redirectUrl
                )
                val response = paymentApiService.initiatePayment(request)
                Log.d(TAG, "initiatePayment response: $response")
                if (response.isSuccessful && response.body() != null) {
                    var finalRedirectUrl = response.body()!!.redirectUrl
                    // Workaround: Append redirectUrl as return_url if the response is the login page
                    if (finalRedirectUrl.contains("79.76.108.164:82/login")) {
                        Log.w(TAG, "Detected payment gateway login URL: $finalRedirectUrl")
                        val encodedRedirectUrl = URLEncoder.encode(redirectUrl, "UTF-8")
                        finalRedirectUrl = "$finalRedirectUrl?return_url=$encodedRedirectUrl"
                        Log.d(TAG, "Modified redirect URL: $finalRedirectUrl")
                    }
                    _paymentState.value = PaymentState.Success
                    onRedirect(finalRedirectUrl)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "initiatePayment failed: HTTP ${response.code()}, Response: $errorBody")
                    _paymentState.value = PaymentState.Error("Payment initiation failed: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "initiatePayment error: ${e.message}", e)
                _paymentState.value = PaymentState.Error(e.message ?: "Network error")
            }
        }
    }

    fun processPaymentAndInitiate(
        address: String,
        phoneNumber: String,
        customerName: String,
        country: String,
        postalCode: String,
        totalAmount: Float,
        redirectUrl: String,
        onRedirect: (String) -> Unit
    ) {
        viewModelScope.launch {
            val payResult = payAndClearCart(address, phoneNumber, customerName, country, postalCode)
            if (payResult.isSuccess) {
                initiatePayment(totalAmount, redirectUrl, onRedirect)
            }
        }
    }

    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
    }

    fun toggleFavorite() {
        _isFavorite.value = !_isFavorite.value
    }
}