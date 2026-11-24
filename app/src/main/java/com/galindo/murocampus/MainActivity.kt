package com.galindo.murocampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.galindo.murocampus.data.PostRepositoryRemote
import com.galindo.murocampus.data.remote.RetrofitClient
import com.galindo.murocampus.ui.PantallaCrear
import com.galindo.murocampus.ui.PantallaDetalle
import com.galindo.murocampus.ui.PantallaLista
import com.galindo.murocampus.ui.theme.MuroCampusTheme
import com.galindo.murocampus.viewmodel.PostsViewModel
import com.galindo.murocampus.viewmodel.PostsViewModelFactory

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MuroCampusTheme {
                val repository = PostRepositoryRemote(RetrofitClient.api)
                val postsViewModel: PostsViewModel = viewModel(
                    factory = PostsViewModelFactory(repository)
                )

                var idSeleccionado by rememberSaveable { mutableStateOf<Int?>(null) }
                var creando by rememberSaveable { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        TopAppBar(
                            title = {
                                val titulo = when {
                                    creando -> "Nueva Publicación"
                                    idSeleccionado != null -> "Detalle de Publicación"
                                    else -> "MuroCampus"
                                }
                                Text(
                                    text = titulo,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                if (creando) {
                                    IconButton(onClick = {
                                        postsViewModel.limpiarCreacion()
                                        creando = false
                                    }) {
                                        Text(
                                            text = "←",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                } else if (idSeleccionado != null) {
                                    IconButton(onClick = { idSeleccionado = null }) {
                                        Text(
                                            text = "←",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    floatingActionButton = {
                        if (idSeleccionado == null && !creando) {
                            FloatingActionButton(
                                onClick = { creando = true },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                            ) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when {
                            creando -> {
                                PantallaCrear(
                                    vm = postsViewModel,
                                    onVolver = { creando = false }
                                )
                            }
                            idSeleccionado != null -> {
                                PantallaDetalle(
                                    id = idSeleccionado!!,
                                    vm = postsViewModel,
                                    onVolver = { idSeleccionado = null }
                                )
                            }
                            else -> {
                                PantallaLista(
                                    vm = postsViewModel,
                                    onSeleccionarPost = { idSeleccionado = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
