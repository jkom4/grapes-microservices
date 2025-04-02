package grapes.microservices

import grapes.microservices.models.api.GrapesApi
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodels.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // singleton
    single {
        GrapesApi()
    }
    single<ArticleRepository> {
        ArticleRepository(get())
    }
    viewModel{
        HomeViewModel(get())
    }
}