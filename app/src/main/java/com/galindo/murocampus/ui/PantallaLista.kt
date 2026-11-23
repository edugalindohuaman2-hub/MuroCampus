package com.galindo.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galindo.murocampus.viewmodel.PostsViewModel

@Composable
fun PantallaLista(
    vm: PostsViewModel,
    onSeleccionarPost: (Int) -> Unit
) {
    val estado by vm.lista.collectAsStateWithLifecycle()
    val filtro by vm.usuarioFiltro.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = (filtro == null),
                    onClick = { vm.filtrar(null) },
                    label = { Text("Todos") }
                )
            }
            items((1..10).toList()) { i ->
                FilterChip(
                    selected = (filtro == i),
                    onClick = { vm.filtrar(i) },
                    label = { Text("Usuario $i") }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
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
                    if (s.datos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin publicaciones",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
        }
    }
}
