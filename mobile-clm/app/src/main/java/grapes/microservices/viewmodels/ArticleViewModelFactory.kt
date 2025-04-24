package grapes.microservices.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import grapes.microservices.models.network.ArticleApiService
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.models.utils.CartManager

class ArticleViewModelFactory(
    private val repository: ArticleRepository,
    private val apiService: ArticleApiService,
    private val cartManager: CartManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticleViewModel(repository, apiService, cartManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun createFactory(
            context: Context,
            repository: ArticleRepository,
            apiService: ArticleApiService
        ): ArticleViewModelFactory {
            val cartManager = CartManager.getInstance(context, apiService)
            return ArticleViewModelFactory(repository, apiService, cartManager)
        }
    }
}