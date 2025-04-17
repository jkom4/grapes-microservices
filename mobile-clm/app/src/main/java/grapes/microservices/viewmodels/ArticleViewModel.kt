package grapes.microservices.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.Cart
import grapes.microservices.models.network.ArticleApiService
import grapes.microservices.models.network.InitCartRequest
import grapes.microservices.models.network.AddToCartRequest
import grapes.microservices.models.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// States for handling article-related operations
sealed class ArticleState {
    object Loading : ArticleState()
    data class Success(val article: Article) : ArticleState()
    data class Error(val message: String) : ArticleState()
}

// States for handling cart-related operations
sealed class CartState {
    object Idle : CartState()
    object Loading : CartState()
    object Success : CartState()
    data class Error(val message: String) : CartState()
}

// States for handling article pagination
sealed class ArticlePaginationState {
    object Loading : ArticlePaginationState()
    data class Success(val articles: List<Article>, val currentPage: Int) : ArticlePaginationState()
    data class Error(val message: String) : ArticlePaginationState()
}

// States for handling the cart screen
sealed class CartScreenState {
    object Loading : CartScreenState()
    data class Success(val cart: Cart) : CartScreenState()
    data class Error(val message: String) : CartScreenState()
}

// States for handling payment operations
sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    object Success : PaymentState()
    data class Error(val message: String) : PaymentState()
}

class ArticleViewModel(
    private val repository: ArticleRepository,
    private val apiService: ArticleApiService
) : ViewModel() {
    // Constants for cart and pagination
    private companion object {
        const val DEFAULT_USER_ID = 1
        const val DEFAULT_ORDER_ID = 1
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

    fun nextPage() {
        currentPage++
        loadArticles()
    }

    fun previousPage() {
        if (currentPage > 0) {
            currentPage--
            loadArticles()
        }
    }

    fun addToCart(articleId: Int, quantityKg: Float, quantityUnit: Float) {
        viewModelScope.launch {
            _cartState.value = CartState.Loading
            try {
                val initResponse = apiService.initCart(InitCartRequest(userId = DEFAULT_USER_ID))
                if (!initResponse.isSuccessful) {
                    _cartState.value = CartState.Error("Error initializing cart")
                    return@launch
                }

                val addResponse = apiService.addToCart(
                    AddToCartRequest(
                        orderId = DEFAULT_ORDER_ID,
                        articleId = articleId,
                        quantityKg = quantityKg,
                        quantity = quantityUnit
                    )
                )
                if (addResponse.isSuccessful) {
                    _cartState.value = CartState.Success
                } else {
                    _cartState.value = CartState.Error("Error adding to cart")
                }
            } catch (e: Exception) {
                _cartState.value = CartState.Error(e.message ?: "Network error")
            }
        }
    }

    fun fetchCart(orderId: Int) {
        viewModelScope.launch {
            _cartScreenState.value = CartScreenState.Loading
            try {
                val cart = apiService.getCart(orderId)
                _cartScreenState.value = CartScreenState.Success(cart)
            } catch (e: Exception) {
                _cartScreenState.value = CartScreenState.Error(e.message ?: "Error fetching cart")
            }
        }
    }

    fun removeFromCart(itemId: Int, orderId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.removeFromCart(itemId)
                if (response.isSuccessful) {
                    fetchCart(orderId)
                } else {
                    _cartScreenState.value = CartScreenState.Error("Error removing item from cart")
                }
            } catch (e: Exception) {
                _cartScreenState.value = CartScreenState.Error(e.message ?: "Network error")
            }
        }
    }

    fun payAndClearCart(
        orderId: Int,
        address: String,
        phoneNumber: String,
        customerName: String,
        country: String,
        postalCode: String
    ) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            try {
                val payResponse = apiService.payCart(
                    orderId = orderId,
                    address = address,
                    phoneNumber = phoneNumber,
                    customerName = customerName,
                    country = country,
                    postalCode = postalCode
                )
                if (!payResponse.isSuccessful) {
                    _paymentState.value = PaymentState.Error("Payment error")
                    return@launch
                }

                val clearResponse = apiService.clearCart(orderId)
                if (!clearResponse.isSuccessful) {
                    _paymentState.value = PaymentState.Error("Error clearing cart")
                    return@launch
                }

                fetchCart(orderId)
                _paymentState.value = PaymentState.Success
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetCartState() {
        _cartState.value = CartState.Idle
    }

    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
    }
}