package grapes.microservices.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import grapes.microservices.models.data.Article
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.models.utils.CartManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel class for managing Home-related data (articles, filters, etc.)
class HomeViewModel(
    private val articleRepo: ArticleRepository, // Repository for fetching articles and related data
    private val cartManager: CartManager
) : ViewModel() {

    // StateFlow to hold the list of articles
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow() // Expose articles list as StateFlow

    // Initialization block to fetch articles as soon as the ViewModel is created
    init {
        fetchArticles()
        resetAndInitializeCart()
    }

    // Function to fetch articles from the repository
    private fun fetchArticles() {
        viewModelScope.launch {
            val fetchedArticles = articleRepo.getArticles() // Fetch articles from the repository
            _articles.value = fetchedArticles // Update the articles list
        }
    }

    private fun resetAndInitializeCart() {
        cartManager.resetAndInitializeCart(userId = 1) // Use appropriate userId
    }
}
