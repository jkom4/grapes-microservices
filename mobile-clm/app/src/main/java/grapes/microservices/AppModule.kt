package grapes.microservices

import grapes.microservices.models.api.GrapesApi
import retrofit2.Retrofit
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodels.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Module for dependency injection using Koin.
 * This module initialize singleton services like API, Repository, ViewModel, etc.
 */
val appModule = module {
    // singleton
    single {
        Retrofit.Builder()
            .baseUrl("https://api.example.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GrapesApi::class.java)
    }
    single<ArticleRepository> {
        ArticleRepository(get())
    }
    viewModel{
        HomeViewModel(get())
    }
}