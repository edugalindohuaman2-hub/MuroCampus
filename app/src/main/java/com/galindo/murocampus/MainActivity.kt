package com.galindo.murocampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.galindo.murocampus.data.PostRepositoryRemote
import com.galindo.murocampus.data.remote.RetrofitClient
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = if (idSeleccionado == null) "MuroCampus" else "Detalle de Publicación",
                                    color = Color.White
                                )
                            },
                            navigationIcon = {
                                if (idSeleccionado != null) {
                                    IconButton(onClick = { idSeleccionado = null }) {
                                        Text(
                                            text = "←",
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val id = idSeleccionado
                        if (id == null) {
                            PantallaLista(
                                vm = postsViewModel,
                                onSeleccionarPost = { idSeleccionado = it }
                            )
                        } else {
                            PantallaDetalle(
                                id = id,
                                vm = postsViewModel,
                                onVolver = { idSeleccionado = null }
                            )
                        }
                    }
                }
            }
        }
    }
}
