package com.galindo.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.galindo.murocampus.data.PostRepository
import com.galindo.murocampus.model.Comentario
import com.galindo.murocampus.model.Post
import com.galindo.murocampus.ui.UiState
import com.galindo.murocampus.ui.mensajeDe
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleUi(
    val post: Post,
    val comentarios: List<Comentario>
)

class PostsViewModel(private val repo: PostRepository) : ViewModel() {

    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista: StateFlow<UiState<List<Post>>> = _lista.asStateFlow()

    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle: StateFlow<UiState<DetalleUi>> = _detalle.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _lista.value = UiState.Cargando
            repo.listar()
                .onSuccess { _lista.value = UiState.Exito(it) }
                .onFailure { _lista.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun abrir(id: Int) {
        viewModelScope.launch {
            _detalle.value = UiState.Cargando
            coroutineScope {
                val postDef = async { repo.detalle(id) }
                val comDef = async { repo.comentarios(id) }

                val postRes = postDef.await()
                val comRes = comDef.await()

                if (postRes.isSuccess && comRes.isSuccess) {
                    _detalle.value = UiState.Exito(
                        DetalleUi(postRes.getOrThrow(), comRes.getOrThrow())
                    )
                } else {
                    val error = postRes.exceptionOrNull() ?: comRes.exceptionOrNull()!!
                    _detalle.value = UiState.Error(mensajeDe(error))
                }
            }
        }
    }
}
