package grapes.microservices.viewmodel

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
        private const val TAG = "ArticleViewModel"
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
        initializeCart()
    }

    private fun initializeCart() {
        viewModelScope.launch {
            try {
                cartManager.initializeCart(DEFAULT_USER_ID)
                cartManager.orderId.collect { orderId ->
                    if (orderId != null) {
                        Log.d(TAG, "Cart initialized with orderId: $orderId")
                        fetchCart()
                    } else {
                        Log.e(TAG, "Cart initialization failed: orderId is null")
                        _cartScreenState.value = CartScreenState.Error("Failed to initialize cart")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing cart: ${e.message}", e)
                _cartScreenState.value = CartScreenState.Error("Error initializing cart: ${e.message}")
            }
        }
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
                    fetchCart()
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
                val calculatedTotalPrice = cart.items.sumOf { item ->
                    if (item.quantityKg > 0) {
                        (item.quantityKg * item.price).toDouble()
                    } else {
                        (item.quantity * item.price).toDouble()
                    }
                }.toFloat()
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
            if (orderId == null) {
                _cartScreenState.value = CartScreenState.Error("Cart not initialized")
                Log.e(TAG, "removeFromCart failed: orderId is null")
                return@launch
            }
            try {
                val response = apiService.removeFromCart(orderId, itemId)
                if (response.isSuccessful) {
                    fetchCart()
                } else {
                    _cartScreenState.value = CartScreenState.Error("Error removing item from cart: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                _cartScreenState.value = CartScreenState.Error(e.message ?: "Network error")
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
        if (orderId == null) {
            Log.e(TAG, "payAndClearCart failed: orderId is null")
            _paymentState.value = PaymentState.Error("Cart not initialized")
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
        _paymentState.value = PaymentState.Loading
        Log.d(TAG, "Sending pay request to /clm/cart/pay: ${payRequest.toJson()}")
        return try {
            val payResponse = apiService.payCart(payRequest)
            Log.d(TAG, "Pay response: HTTP ${payResponse.code()}, Body: ${payResponse.body()}")
            if (payResponse.isSuccessful) {
                cartManager.clearCart()
                _paymentState.value = PaymentState.Success
                Log.d(TAG, "Pay and clear cart successful")
                Result.success(Unit)
            } else {
                val errorBody = payResponse.errorBody()?.string() ?: "No error body"
                val errorMessage = "Payment error: HTTP ${payResponse.code()}, Response: $errorBody"
                Log.e(TAG, errorMessage)
                _paymentState.value = PaymentState.Error(errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "payAndClearCart error: ${e.message}", e)
            _paymentState.value = PaymentState.Error(e.message ?: "Network error")
            Result.failure(e)
        }
    }

    private fun PayCartRequest.toJson(): String {
        return """
            {
                "orderId": $orderId,
                "customerName": "$customerName",
                "phoneNumber": "$phoneNumber",
                "address": "$address",
                "country": "$country",
                "postalCode": "$postalCode"
            }
        """.trimIndent()
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
                Log.d(TAG, "Sending payment initiation request to /payment/login/payment-initiate")
                Log.d(TAG, "JSON data: ${request.toJson()}")
                val response = paymentApiService.initiatePayment(request)
                Log.d(TAG, "Payment initiation response: HTTP ${response.code()}, Body: ${response.body()}")
                if (response.isSuccessful) {
                    if (response.body() != null && response.body()!!.isSuccess) {
                        val finalRedirectUrl = response.body()!!.redirectUrl
                        Log.d(TAG, "Payment initiation successful, redirectUrl: $finalRedirectUrl")
                        _paymentState.value = PaymentState.Success
                        onRedirect(finalRedirectUrl)
                        testSessionDetails()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: response.body()?.message ?: "No body or invalid response"
                        Log.e(TAG, "Payment initiation failed: HTTP ${response.code()}, Response: $errorBody")
                        _paymentState.value = PaymentState.Error("Invalid response: $errorBody")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "Payment initiation failed: HTTP ${response.code()}, Response: $errorBody")
                    _paymentState.value = PaymentState.Error("Payment initiation failed: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Payment initiation error: ${e.message}", e)
                _paymentState.value = PaymentState.Error(e.message ?: "Network error")
            }
        }
    }

    private fun PaymentInitiateRequest.toJson(): String {
        return """
            {
                "amount": $amount,
                "merchantId": "$merchantId",
                "redirectUrl": "$redirectUrl"
            }
        """.trimIndent()
    }

    fun testSessionDetails() {
        viewModelScope.launch {
            try {
                val response = paymentApiService.getSessionDetails()
                Log.d(TAG, "Session details response: HTTP ${response.code()}, Body: ${response.body()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "Session details: amount=${response.body()?.amount}, merchant=${response.body()?.merchantName}, redirectUrl=${response.body()?.redirectUrl}")
                } else {
                    Log.e(TAG, "Failed to get session details: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching session details: ${e.message}", e)
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
        merchantId: String = "grapes",
        redirectUrl: String,
        onRedirect: (String) -> Unit
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Starting processPaymentAndInitiate with amount: $totalAmount, merchantId: $merchantId, redirectUrl: $redirectUrl")
            val payResult = payAndClearCart(address, phoneNumber, customerName, country, postalCode)
            if (payResult.isSuccess) {
                initiatePayment(totalAmount, redirectUrl, onRedirect)
            } else {
                _paymentState.value = PaymentState.Error("Payment processing failed")
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