package grapes.microservices

import android.content.Context
import grapes.microservices.models.network.ArticleApiService
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.models.utils.CartManager
import grapes.microservices.viewmodels.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // singleton
    single<ArticleApiService> {
        RetrofitClient.articleApiService
    }

    // Singleton for ArticleRepository
    single<ArticleRepository> {
        ArticleRepository(get<ArticleApiService>())
    }

    // Singleton for CartManager
    single<CartManager> { (context: Context) ->
        CartManager.getInstance(context, get<ArticleApiService>())
    }
    viewModel {
        HomeViewModel(
            articleRepo = get<ArticleRepository>(),
            cartManager = get<CartManager>(parameters = { org.koin.core.parameter.parametersOf(get<Context>()) })
        )
    }
}