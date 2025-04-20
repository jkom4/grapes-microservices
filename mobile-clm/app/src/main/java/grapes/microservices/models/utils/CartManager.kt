package grapes.microservices.models.utils

import android.content.Context
import android.util.Log
import grapes.microservices.models.data.InitCartRequest
import grapes.microservices.models.network.ArticleApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "CartManager"

class CartManager private constructor(
    private val apiService: ArticleApiService,
    private val cartDataStore: CartDataStore
) {
    private val _orderId = MutableStateFlow<Int?>(null)
    val orderId: StateFlow<Int?> = _orderId

    init {
        // Load orderId from the Datastore
        CoroutineScope(Dispatchers.IO).launch {
            val storedOrderId = cartDataStore.getOrderId()
            _orderId.value = storedOrderId
            Log.d(TAG, "Initialized with stored orderId: $storedOrderId")
        }
    }

    fun initializeCart(userId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (_orderId.value == null) {
                try {
                    val response = apiService.initCart(InitCartRequest(userId = 1))
                    if (response.isSuccessful) {
                        val newOrderId = response.body()?.id
                        if (newOrderId != null) {
                            _orderId.value = newOrderId
                            cartDataStore.saveOrderId(newOrderId)
                            Log.d(TAG, "Initialized new orderId from API: $newOrderId")
                        } else {
                            Log.e(TAG, "Failed to initialize cart: response body has no id")
                        }
                    } else {
                        Log.e(TAG, "Failed to initialize cart: HTTP ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing cart: ${e.message}")
                }
            } else {
                Log.d(TAG, "Cart already initialized with orderId: ${_orderId.value}")
            }
        }
    }

    fun clearCart() {
        CoroutineScope(Dispatchers.IO).launch {
            _orderId.value?.let { orderId ->
                try {
                    apiService.clearCart(orderId)
                    _orderId.value = null
                    cartDataStore.clearOrderId()
                    Log.d(TAG, "Cleared cart with orderId: $orderId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing cart: ${e.message}")
                }
            } ?: Log.d(TAG, "No orderId to clear")
        }
    }

    companion object {
        @Volatile
        private var instance: CartManager? = null

        fun getInstance(context: Context, apiService: ArticleApiService): CartManager {
            return instance ?: synchronized(this) {
                instance ?: CartManager(apiService, CartDataStore(context)).also { instance = it }
            }
        }
    }
}