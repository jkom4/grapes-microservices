package grapes.microservices

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import grapes.microservices.models.api.GrapesApi
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodels.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val appModule = module {

    // Moshi converter pour gérer le JSON
    single {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    // Retrofit pour créer l'instance de l'API
    single {
        Retrofit.Builder()
            .baseUrl("http://localhost:8092/")  // Assure-toi que l'URL de base est correcte
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    // Fournir l'instance de GrapesApi à partir de Retrofit
    single {
        get<Retrofit>().create(GrapesApi::class.java)
    }

    // Repository
    single<ArticleRepository> {
        ArticleRepository(get())
    }

    // ViewModel
    viewModel {
        HomeViewModel(get())
    }
}
