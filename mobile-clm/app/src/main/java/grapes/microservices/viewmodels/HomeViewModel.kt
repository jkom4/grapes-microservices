package grapes.microservices.viewmodels.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.ArticleFilter
import grapes.microservices.models.data.ArticleMinMaxPrice
import grapes.microservices.models.repository.ArticleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val articleRepo: ArticleRepository
) : ViewModel() {
    // --- State ---
    private val _state = mutableStateOf<HomeState<Unit>>(HomeState.Loading)
    val state: State<HomeState<Unit>> = _state

    // --- Attributes ---
    private val _filterSettings = MutableStateFlow<ArticleFilter?>(null)
    val filterSettings: StateFlow<ArticleFilter?> = _filterSettings.asStateFlow()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> get() = _articles.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> get() = _categories.asStateFlow()

    private val _families = MutableStateFlow<List<String>>(emptyList())
    val families: StateFlow<List<String>> get() = _families.asStateFlow()

    // code executed on initialization
    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            _articles.value = emptyList() // Optionnel: vider les articles pendant le chargement

            try {
                delay(1000)
                // Change MinMaxCost and init filters
                val minMaxCost = articleRepo.getMinMaxCost()
                // Update only when ready
                _filterSettings.value = ArticleFilter(priceRange = ArticleMinMaxPrice(minMaxCost))

                fetchArticles()
                fetchCategories()
                fetchFamilies()

                // Set state to success
                _state.value = HomeState.Success(Unit)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "unknown_error"
                _state.value = HomeState.Error(errorMessage)
                _filterSettings.value = null
            }
        }
    }

    private suspend fun fetchArticles() {
        val currentFilters = _filterSettings.value
        if (currentFilters != null) {
            _articles.value = articleRepo.getArticles(currentFilters)
        } else {
            _articles.value = emptyList()
        }
    }

    private suspend fun fetchCategories() {
        _categories.value = articleRepo.getCategories().map { it.name.orEmpty() }
    }

    private suspend fun fetchFamilies() {
        _families.value = articleRepo.getFamilies().map { it.name.orEmpty() }
    }

    fun updateFilters(newFilterData: ArticleFilter) {
        val currentFilterValue = _filterSettings.value ?: return // Ne rien faire si pas encore initialisé

        // Crée une copie mise à jour basée sur l'état actuel
        val updatedFilter = currentFilterValue.copy(
            priceRange = newFilterData.priceRange,
            rattingRange = newFilterData.rattingRange,
            query = newFilterData.query,
            category = newFilterData.category,
            family = newFilterData.family
        )

        // Update only if something changed
        if (currentFilterValue.sameAs(updatedFilter)) return
        _filterSettings.value = updatedFilter

        viewModelScope.launch {
            _state.value = HomeState.Loading
            delay(1000)
            fetchArticles()
            _state.value = HomeState.Success(Unit)
        }
    }
}