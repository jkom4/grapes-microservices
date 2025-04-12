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
    object Loading : ArticleState() // Loading state while fetching article data
    data class Success(val article: Article) : ArticleState() // Success state with the fetched article
    data class Error(val message: String) : ArticleState() // Error state with an error message
}

// States for handling cart-related operations
sealed class CartState {
    object Idle : CartState() // Idle state when no action is being performed
    object Loading : CartState() // Loading state when cart is being processed
    object Success : CartState() // Success state when cart operation succeeds
    data class Error(val message: String) : CartState() // Error state with an error message
}

// States for handling article pagination
sealed class ArticlePaginationState {
    object Loading : ArticlePaginationState() // Loading state for article pagination
    data class Success(val articles: List<Article>, val currentPage: Int) : ArticlePaginationState() // Success state with the articles and current page
    data class Error(val message: String) : ArticlePaginationState() // Error state with an error message
}

// States for handling the cart screen
sealed class CartScreenState {
    object Loading : CartScreenState() // Loading state for the cart screen
    data class Success(val cart: Cart) : CartScreenState() // Success state with the fetched cart
    data class Error(val message: String) : CartScreenState() // Error state with an error message
}

// States for handling payment operations
sealed class PaymentState {
    object Idle : PaymentState() // Idle state when no payment action is being performed
    object Loading : PaymentState() // Loading state while processing payment
    object Success : PaymentState() // Success state when payment is successful
    data class Error(val message: String) : PaymentState() // Error state with an error message
}

class ArticleViewModel(
    private val repository: ArticleRepository, // Repository to handle article-related data operations
    private val apiService: ArticleApiService // API service to handle network requests
) : ViewModel() {
    // MutableStateFlow to track the current state of the article
    private val _articleState = MutableStateFlow<ArticleState>(ArticleState.Loading)
    val articleState: StateFlow<ArticleState> = _articleState // Expose article state

    // MutableStateFlow to track the current state of the cart operations
    private val _cartState = MutableStateFlow<CartState>(CartState.Idle)
    val cartState: StateFlow<CartState> = _cartState // Expose cart state

    // MutableStateFlow to track the current state of the cart screen
    private val _cartScreenState = MutableStateFlow<CartScreenState>(CartScreenState.Loading)
    val cartScreenState: StateFlow<CartScreenState> = _cartScreenState // Expose cart screen state

    // MutableStateFlow to track the current state of the payment operation
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState // Expose payment state

    // MutableStateFlow to track pagination of articles
    private val _articlePaginationState = MutableStateFlow<ArticlePaginationState>(ArticlePaginationState.Loading)
    val articlePaginationState: StateFlow<ArticlePaginationState> = _articlePaginationState // Expose article pagination state

    private var currentPage = 0 // Track the current page for pagination
    private val pageSize = 20 // Number of articles per page

    // Initialize and load articles when the ViewModel is created
    init {
        loadArticles()
    }

    // Fetch an article by its ID
    fun fetchArticleById(id: Int) {
        viewModelScope.launch {
            _articleState.value = ArticleState.Loading // Set loading state while fetching the article
            val result = repository.getArticleById(id)
            _articleState.value = when {
                result.isSuccess -> ArticleState.Success(result.getOrNull()!!) // Success: update with the article data
                result.isFailure -> ArticleState.Error(result.exceptionOrNull()?.message ?: "Unknown error") // Failure: update with error message
                else -> ArticleState.Error("Unexpected result state") // Unexpected result state
            }
        }
    }

    // Load articles from the repository (for pagination)
    fun loadArticles() {
        viewModelScope.launch {
            _articlePaginationState.value = ArticlePaginationState.Loading // Set loading state
            try {
                val articles = repository.getArticles()
                if (articles.isNotEmpty()) {
                    _articlePaginationState.value = ArticlePaginationState.Success(articles, currentPage) // Success: update with articles and current page
                } else {
                    _articlePaginationState.value = ArticlePaginationState.Error("No more articles available.") // Error: no more articles available
                }
            } catch (e: Exception) {
                _articlePaginationState.value = ArticlePaginationState.Error("Error loading articles: ${e.message}") // Error: catch and show error message
            }
        }
    }

    // Fetch the next page of articles
    fun nextPage() {
        currentPage++ // Increment current page
        loadArticles() // Load articles for the new page
    }

    // Fetch the previous page of articles
    fun previousPage() {
        if (currentPage > 0) {
            currentPage-- // Decrement current page
            loadArticles() // Load articles for the new page
        }
    }

    // Add an article to the cart
    fun addToCart(articleId: Int, quantityKg: Float, quantityUnit: Float) {
        viewModelScope.launch {
            _cartState.value = CartState.Loading // Set loading state while adding to the cart
            try {
                // Initialize the cart
                val initResponse = apiService.initCart(InitCartRequest(userId = 1))
                if (!initResponse.isSuccessful) {
                    _cartState.value = CartState.Error("Error initializing cart") // Error: initialization failed
                    return@launch
                }

                // Add the article to the cart
                val addResponse = apiService.addToCart(
                    AddToCartRequest(
                        orderId = 1,
                        articleId = articleId,
                        quantityKg = quantityKg,
                        quantity = quantityUnit
                    )
                )
                if (addResponse.isSuccessful) {
                    _cartState.value = CartState.Success // Success: article added to cart
                } else {
                    _cartState.value = CartState.Error("Error adding to cart") // Error: failed to add to cart
                }
            } catch (e: Exception) {
                _cartState.value = CartState.Error(e.message ?: "Network error") // Error: catch network error
            }
        }
    }

    // Fetch the cart data by order ID
    fun fetchCart(orderId: Int) {
        viewModelScope.launch {
            _cartScreenState.value = CartScreenState.Loading // Set loading state
            try {
                val cart = apiService.getCart(orderId)
                _cartScreenState.value = CartScreenState.Success(cart) // Success: update with the fetched cart
            } catch (e: Exception) {
                _cartScreenState.value = CartScreenState.Error(e.message ?: "Error fetching cart") // Error: failed to fetch cart
            }
        }
    }

    // Remove an item from the cart
    fun removeFromCart(itemId: Int, orderId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.removeFromCart(itemId)
                if (response.isSuccessful) {
                    fetchCart(orderId) // Refresh the cart after removal
                } else {
                    _cartScreenState.value = CartScreenState.Error("Error removing item from cart") // Error: failed to remove item
                }
            } catch (e: Exception) {
                _cartScreenState.value = CartScreenState.Error(e.message ?: "Network error") // Error: catch network error
            }
        }
    }

    // Process payment and clear the cart
    fun payAndClearCart(orderId: Int) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading // Set loading state while processing payment
            try {
                // Process the payment
                val payResponse = apiService.payCart(orderId)
                if (!payResponse.isSuccessful) {
                    _paymentState.value = PaymentState.Error("Error during payment") // Error: payment failed
                    return@launch
                }

                // Clear the cart after payment
                val clearResponse = apiService.clearCart(orderId)
                if (clearResponse.isSuccessful) {
                    _paymentState.value = PaymentState.Success // Success: payment and cart cleared
                } else {
                    _paymentState.value = PaymentState.Error("Error clearing cart") // Error: failed to clear cart
                }
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error(e.message ?: "Network error") // Error: network issue
            }
        }
    }

    // Reset the cart state to Idle
    fun resetCartState() {
        _cartState.value = CartState.Idle
    }

    // Reset the payment state to Idle
    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
    }
}
