package com.galindo.murocampus.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galindo.murocampus.viewmodel.PostsViewModel

@Composable
fun PantallaLista(
    vm: PostsViewModel,
    onSeleccionarPost: (Int) -> Unit
) {
    val estado by vm.lista.collectAsStateWithLifecycle()

    when (val s = estado) {
        is UiState.Cargando -> {
            Cargando("Cargando publicaciones...")
        }
        is UiState.Error -> {
            ErrorConReintento(
                mensaje = s.mensaje,
                onReintentar = { vm.cargar() }
            )
        }
        is UiState.Exito -> {
            LazyColumn {
                items(s.datos, key = { it.id }) { post ->
                    TarjetaPost(
                        post = post,
                        onClick = { onSeleccionarPost(post.id) }
                    )
                }
            }
        }
    }
}
