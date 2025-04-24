package grapes.microservices.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.models.utils.CartManager
import android.content.Context

class HomeViewModelFactory(
    private val repository: ArticleRepository,
    private val cartManager: CartManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, cartManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun create(context: Context): HomeViewModelFactory {
            val repository = ArticleRepository(RetrofitClient.articleApiService)
            val cartManager = CartManager.getInstance(context, RetrofitClient.articleApiService)
            return HomeViewModelFactory(repository, cartManager)
        }
    }
}