package com.galindo.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    val consulta by vm.consulta.collectAsStateWithLifecycle()

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
                    label = { Text("Todos") },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            items((1..10).toList()) { i ->
                FilterChip(
                    selected = (filtro == i),
                    onClick = { vm.filtrar(i) },
                    label = { Text("Usuario $i") },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        OutlinedTextField(
            value = consulta,
            onValueChange = { vm.buscar(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("Buscar por título...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            trailingIcon = {
                if (consulta.isNotEmpty()) {
                    IconButton(onClick = { vm.buscar("") }) {
                        Text(
                            text = "✕",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Sin publicaciones",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No hay registros para este filtro o búsqueda",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
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
