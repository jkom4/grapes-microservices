package grapes.microservices.viewmodel

import androidx.lifecycle.ViewModel
import grapes.microservices.model.repository.ArticleRepository

class HomeViewModel constructor(private val articleRepo: ArticleRepository = ArticleRepository()) : ViewModel() {

}