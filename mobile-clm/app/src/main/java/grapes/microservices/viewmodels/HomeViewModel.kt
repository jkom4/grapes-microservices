package grapes.microservices.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.ArticleFilterSettings
import grapes.microservices.models.data.ArticleMinMaxPrice
import grapes.microservices.models.repository.ArticleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val articleRepo: ArticleRepository
) : ViewModel() {

    // Flow pour les articles
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    // Flow pour les paramètres de filtre
    private val _filterSettings = MutableStateFlow(
        ArticleFilterSettings(articleMinMaxPrice = ArticleMinMaxPrice(
            articleRepo.getMinMaxCost(),
            articleRepo.getMinMaxCost()
        ))
    )

    fun getCategories(): List<String> {
        return articleRepo.getCategories().values.map { it.name.orEmpty() }
    }

    fun getFamilies(): List<String> {
        return articleRepo.getFamilies().values.map { it.name.orEmpty() }
    }

    val filterSettings: StateFlow<ArticleFilterSettings> = _filterSettings.asStateFlow()

    // State pour afficher l'état de chargement
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        // Charger les articles au lancement de la ViewModel
        loadArticles()
    }

    private fun loadArticles() {
        viewModelScope.launch {
            try {
                // Appel à l'API pour récupérer les articles
                _articles.value = articleRepo.getArticles()
            } catch (e: Exception) {
                // Gérer les erreurs
                _articles.value = emptyList() // ou une liste d'articles par défaut
            }
        }
    }

    // Méthode pour mettre à jour les paramètres de filtre
    fun updateCategory(newCategory: String) {
        _filterSettings.update { currentState ->
            val categoryToSet = if (currentState.category == newCategory) "" else newCategory
            currentState.copy(category = categoryToSet)
        }
        loadArticles() // Recharger les articles avec le filtre mis à jour
    }

    fun updateFamily(newFamily: String) {
        _filterSettings.update { currentState ->
            val familyToSet = if (currentState.family == newFamily) "" else newFamily
            currentState.copy(family = familyToSet)
        }
        loadArticles() // Recharger les articles avec le filtre mis à jour
    }

    fun updateQuery(newQuery: String) {
        _filterSettings.update { it.copy(query = newQuery) }
        loadArticles() // Recharger les articles avec le filtre mis à jour
    }

    fun updatePriceRange(newRange: ClosedFloatingPointRange<Float>) {
        _filterSettings.update {
            it.copy(articleMinMaxPrice = it.articleMinMaxPrice.copy(currentInterval = newRange))
        }
        loadArticles() // Recharger les articles avec le filtre mis à jour
    }

    fun updateRattingRange(newRange: ClosedFloatingPointRange<Float>) {
        _filterSettings.update {
            it.copy(articleMinMaxRatting = it.articleMinMaxRatting.copy(currentInterval = newRange))
        }
        loadArticles() // Recharger les articles avec le filtre mis à jour
    }
}
