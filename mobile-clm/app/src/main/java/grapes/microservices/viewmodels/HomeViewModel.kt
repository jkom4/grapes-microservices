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

class HomeViewModel(
    private val articleRepo: ArticleRepository
) : ViewModel() {
    private val _filterSettings by lazy {
        MutableStateFlow(
            ArticleFilterSettings(
                articleMinMaxPrice = ArticleMinMaxPrice(
                    articleRepo.getMinMaxCost(),
                    articleRepo.getMinMaxCost()
                )
            )
        )
    }
    val filterSettings: StateFlow<ArticleFilterSettings> = _filterSettings.asStateFlow()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    init {
        fetchArticles()
    }

    private fun fetchArticles() {
        viewModelScope.launch {
            val fetchedArticles = articleRepo.getArticles()
            _articles.value = fetchedArticles
        }
    }

    fun getFilterSettings(): ArticleFilterSettings {
        return _filterSettings.asStateFlow().value
    }

    fun getCategories(): List<String> {
        return articleRepo.getCategories().values.map { it.name.orEmpty() }
    }

    fun getFamilies(): List<String> {
        return articleRepo.getFamilies().values.map { it.name.orEmpty() }
    }

    fun updateCategory(newCategory: String) {
        _filterSettings.update { currentState ->
            val categoryToSet = if (currentState.category == newCategory) "" else newCategory
            currentState.copy(category = categoryToSet)
        }
    }

    fun updateFamily(newFamily: String) {
        _filterSettings.update { currentState ->
            val familyToSet = if (currentState.family == newFamily) "" else newFamily
            currentState.copy(family = familyToSet)
        }
    }

    fun updateQuery(newQuery: String) {
        _filterSettings.update { it.copy(query = newQuery) }
    }

    fun updatePriceRange(newRange: ClosedFloatingPointRange<Float>) {
        _filterSettings.update {
            it.copy(articleMinMaxPrice = it.articleMinMaxPrice.copy(currentInterval = newRange))
        }
    }

    fun updateRattingRange(newRange: ClosedFloatingPointRange<Float>) {
        _filterSettings.update {
            it.copy(articleMinMaxRatting = it.articleMinMaxRatting.copy(currentInterval = newRange))
        }
    }
}