package grapes.microservices.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import grapes.microservices.models.data.Order
import grapes.microservices.models.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class OrderHistoryViewModel(
    private val orderRepo: OrderRepository
) : ViewModel() {
    // Store all fetched orders locally
    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())

    // Filtered and paginated orders to display
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    private val _codeFilter = MutableStateFlow<String?>(null)
    private val _dateFilter = MutableStateFlow<String?>(null)

    private val pageSize = 20

    init {
        fetchAllOrders()
    }

    // Fetch all orders once (or in large batches)
    private fun fetchAllOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch a large number of orders to minimize API calls
                val fetchedOrders = orderRepo.getOrderHistory(
                    userId = 1,
                    page = 0,
                    size = 1000 // Adjust based on your needs
                )
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                _allOrders.value = fetchedOrders.sortedByDescending { order ->
                    try {
                        dateFormat.parse(order.createdAt)?.time ?: 0
                    } catch (e: Exception) {
                        0
                    }
                }
                applyFiltersAndPaginate()
            } catch (e: Exception) {
                _error.value = "Failed to fetch orders: ${e.message}"
                _allOrders.value = emptyList()
                _orders.value = emptyList()
                _hasMorePages.value = false
            }
            _isLoading.value = false
        }
    }

    // Apply filters and paginate the local data
    private fun applyFiltersAndPaginate(page: Int = 0) {
        val codeFilter = _codeFilter.value
        val dateFilter = _dateFilter.value

        // Filter orders locally
        val filteredOrders = _allOrders.value.filter { order ->
            val codeMatches = codeFilter?.let { filter ->
                order.code.toString().startsWith(filter)
            } ?: true
            val dateMatches = dateFilter?.let { filter ->
                order.createdAt.startsWith(filter)
            } ?: true
            codeMatches && dateMatches
        }

        // Update pagination
        _currentPage.value = page
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, filteredOrders.size)
        _orders.value = filteredOrders.subList(
            startIndex.coerceAtMost(filteredOrders.size),
            endIndex
        )
        _hasMorePages.value = endIndex < filteredOrders.size

        // Show error if no orders match filters
        if (filteredOrders.isEmpty() && (codeFilter != null || dateFilter != null)) {
            _error.value = "No orders match the specified filters"
        } else {
            _error.value = null
        }
    }

    fun applyFilters(code: String?, date: String?) {
        _codeFilter.value = code?.takeIf { it.isNotBlank() }
        _dateFilter.value = date?.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
        applyFiltersAndPaginate(page = 0) // Reset to first page
    }

    fun nextPage() {
        if (_hasMorePages.value) {
            applyFiltersAndPaginate(_currentPage.value + 1)
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            applyFiltersAndPaginate(_currentPage.value - 1)
        }
    }
}