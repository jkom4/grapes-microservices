package grapes.microservices

import grapes.microservices.models.network.ArticleApiService
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodels.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // singleton
    single<ArticleApiService> {
        RetrofitClient.articleApiService
    }
    viewModel{
        HomeViewModel(get())
    }
}