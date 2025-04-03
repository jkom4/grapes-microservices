package grapes.microservices.viewmodels

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.ArticleFilterSettings
import grapes.microservices.models.data.ArticleMinMaxPrice
import grapes.microservices.models.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val articleRepo: ArticleRepository
) : ViewModel() {
    // lazy: initialize only when called for the first tome
    private val _filterSettings by lazy {
        // i don't know the min and max cost among all the articles, so i ask to the API
        MutableStateFlow(ArticleFilterSettings(articleMinMaxPrice = ArticleMinMaxPrice(
            articleRepo.getMinMaxCost(),
            articleRepo.getMinMaxCost()
        )))
    }
    val filterSettings: StateFlow<ArticleFilterSettings> = _filterSettings.asStateFlow()

    fun getFilterSettings(): ArticleFilterSettings {
        return _filterSettings.asStateFlow().value
    }

    fun getArticles(): List<Article> {
        return articleRepo.getArticles()
    }

    fun getCategories(): List<String> {
        return articleRepo.getCategories().values.map { it.name.orEmpty() }
    }

    fun getFamilies(): List<String> {
        return articleRepo.getFamilies().values.map { it.name.orEmpty() }
    }

    fun updateCategory(newCategory: String) {
        _filterSettings.update { currentState ->
            // if select already selected item, deselect it
            val categoryToSet = if (currentState.category == newCategory) "" else newCategory
            currentState.copy(category = categoryToSet) // Crée une nouvelle instance avec la catégorie mise à jour
        }
    }

    fun updateFamily(newFamily: String) {
        _filterSettings.update { currentState ->
            // if select already selected item, deselect it
            val familyToSet = if (currentState.family == newFamily) "" else newFamily
            currentState.copy(family = familyToSet) // Crée une nouvelle instance avec la famille mise à jour
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