package grapes.microservices.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.ArticleFilterSettings
import grapes.microservices.models.data.ArticleMinMaxPrice
import grapes.microservices.models.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel class for managing Home-related data (articles, filters, etc.)
class HomeViewModel(
    private val articleRepo: ArticleRepository // Repository for fetching articles and related data
) : ViewModel() {

    // StateFlow to manage the filter settings (price range, category, etc.)
    private val _filterSettings by lazy {
        MutableStateFlow(
            ArticleFilterSettings(
                articleMinMaxPrice = ArticleMinMaxPrice(
                    articleRepo.getMinMaxCost(), // Get the min and max cost for the articles
                    articleRepo.getMinMaxCost() // This is duplicated, it can be optimized later
                )
            )
        )
    }
    val filterSettings: StateFlow<ArticleFilterSettings> = _filterSettings.asStateFlow() // Expose the filter settings as StateFlow

    // StateFlow to hold the list of articles
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow() // Expose articles list as StateFlow

    // Initialization block to fetch articles as soon as the ViewModel is created
    init {
        fetchArticles()
    }

    // Function to fetch articles from the repository
    private fun fetchArticles() {
        viewModelScope.launch {
            val fetchedArticles = articleRepo.getArticles() // Fetch articles from the repository
            _articles.value = fetchedArticles // Update the articles list
        }
    }

    // Function to retrieve the current filter settings
    fun getFilterSettings(): ArticleFilterSettings {
        return _filterSettings.asStateFlow().value
    }

    // Function to retrieve the list of categories from the repository
    fun getCategories(): List<String> {
        return articleRepo.getCategories().values.map { it.name.orEmpty() } // Map to a list of category names
    }

    // Function to retrieve the list of families from the repository
    fun getFamilies(): List<String> {
        return articleRepo.getFamilies().values.map { it.name.orEmpty() } // Map to a list of family names
    }

    // Function to update the selected category filter
    fun updateCategory(newCategory: String) {
        _filterSettings.update { currentState ->
            val categoryToSet = if (currentState.category == newCategory) "" else newCategory // Toggle category selection
            currentState.copy(category = categoryToSet) // Update the filter settings with the new category
        }
    }

    // Function to update the selected family filter
    fun updateFamily(newFamily: String) {
        _filterSettings.update { currentState ->
            val familyToSet = if (currentState.family == newFamily) "" else newFamily // Toggle family selection
            currentState.copy(family = familyToSet) // Update the filter settings with the new family
        }
    }

    // Function to update the search query in the filter settings
    fun updateQuery(newQuery: String) {
        _filterSettings.update { it.copy(query = newQuery) } // Update the filter settings with the new search query
    }

    // Function to update the price range filter
    fun updatePriceRange(newRange: ClosedFloatingPointRange<Float>) {
        _filterSettings.update {
            it.copy(articleMinMaxPrice = it.articleMinMaxPrice.copy(currentInterval = newRange)) // Update the price range in filter settings
        }
    }

    // Function to update the rating range filter
    fun updateRattingRange(newRange: ClosedFloatingPointRange<Float>) {
        _filterSettings.update {
            it.copy(articleMinMaxRatting = it.articleMinMaxRatting.copy(currentInterval = newRange)) // Update the rating range in filter settings
        }
    }
}
