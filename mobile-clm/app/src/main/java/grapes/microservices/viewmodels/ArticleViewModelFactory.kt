package grapes.microservices.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import grapes.microservices.models.network.ArticleApiService
import grapes.microservices.models.network.PaymentApiService
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.models.utils.CartManager

class ArticleViewModelFactory(
    private val repository: ArticleRepository,
    private val articleApiService: ArticleApiService,
    private val paymentApiService: PaymentApiService,
    private val cartManager: CartManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticleViewModel(repository, articleApiService, paymentApiService, cartManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun createFactory(
            context: Context,
            repository: ArticleRepository,
            articleApiService: ArticleApiService,
            paymentApiService: PaymentApiService
        ): ArticleViewModelFactory {
            val cartManager = CartManager.getInstance(context, articleApiService)
            return ArticleViewModelFactory(repository, articleApiService, paymentApiService, cartManager)
        }
    }
}