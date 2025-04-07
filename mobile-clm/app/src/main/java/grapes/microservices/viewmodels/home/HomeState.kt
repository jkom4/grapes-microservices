package grapes.microservices.viewmodels.home

sealed class HomeState<out T> {
    object Loading : HomeState<Nothing>()
    data class Success<T>(val data: T) : HomeState<T>()
    data class Error(val message: String) : HomeState<Nothing>()
}