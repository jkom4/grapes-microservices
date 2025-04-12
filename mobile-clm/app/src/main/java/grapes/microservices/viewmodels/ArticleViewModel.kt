package grapes.microservices.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

// États pour l'article
sealed class ArticleState {
    object Loading : ArticleState()
    data class Success(val article: Article) : ArticleState()
    data class Error(val message: String) : ArticleState()
}

// États pour l'ajout au panier
sealed class CartState {
    object Idle : CartState()
    object Loading : CartState()
    object Success : CartState()
    data class Error(val message: String) : CartState()
}

// États pour l'écran du panier
sealed class CartScreenState {
    object Loading : CartScreenState()
    data class Success(val cart: Cart) : CartScreenState()
    data class Error(val message: String) : CartScreenState()
}

// États pour le paiement
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
    private val _articleState = MutableStateFlow<ArticleState>(ArticleState.Loading)
    val articleState: StateFlow<ArticleState> = _articleState

    private val _cartState = MutableStateFlow<CartState>(CartState.Idle)
    val cartState: StateFlow<CartState> = _cartState

    private val _cartScreenState = MutableStateFlow<CartScreenState>(CartScreenState.Loading)
    val cartScreenState: StateFlow<CartScreenState> = _cartScreenState

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    fun fetchArticleById(id: Int) {
        viewModelScope.launch {
            _articleState.value = ArticleState.Loading
            val result = repository.getArticleById(id)
            _articleState.value = when {
                result.isSuccess -> ArticleState.Success(result.getOrNull()!!)
                result.isFailure -> ArticleState.Error(
                    result.exceptionOrNull()?.message ?: "Unknown error"
                )
                else -> ArticleState.Error("Unexpected result state")
            }
        }
    }

    fun addToCart(articleId: Int, quantityKg: Float, quantityUnit: Float) {
        viewModelScope.launch {
            _cartState.value = CartState.Loading
            try {
                val initResponse = apiService.initCart(InitCartRequest(userId = 1))
                if (!initResponse.isSuccessful) {
                    _cartState.value = CartState.Error("Erreur lors de l'initialisation du panier")
                    return@launch
                }

                val addResponse = apiService.addToCart(
                    AddToCartRequest(
                        orderId = 1,
                        articleId = articleId,
                        quantityKg = quantityKg,
                        quantity = quantityUnit
                    )
                )
                if (addResponse.isSuccessful) {
                    _cartState.value = CartState.Success
                } else {
                    _cartState.value = CartState.Error("Erreur lors de l'ajout au panier")
                }
            } catch (e: Exception) {
                _cartState.value = CartState.Error(e.message ?: "Erreur réseau")
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
                _cartScreenState.value = CartScreenState.Error(e.message ?: "Erreur lors de la récupération du panier")
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
                    _cartScreenState.value = CartScreenState.Error("Erreur lors de la suppression de l'article")
                }
            } catch (e: Exception) {
                _cartScreenState.value = CartScreenState.Error(e.message ?: "Erreur réseau")
            }
        }
    }

    fun payAndClearCart(orderId: Int) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            try {
                // Étape 1 : Payer le panier
                val payResponse = apiService.payCart(orderId)
                if (!payResponse.isSuccessful) {
                    _paymentState.value = PaymentState.Error("Erreur lors du paiement")
                    return@launch
                }

                // Étape 2 : Vider le panier
                val clearResponse = apiService.clearCart(orderId)
                if (clearResponse.isSuccessful) {
                    _paymentState.value = PaymentState.Success
                } else {
                    _paymentState.value = PaymentState.Error("Erreur lors du vidage du panier")
                }
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error(e.message ?: "Erreur réseau")
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
