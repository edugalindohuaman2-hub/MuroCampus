package com.galindo.murocampus

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.galindo.murocampus.data.PostRepositoryRemote
import com.galindo.murocampus.data.remote.RetrofitClient
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("MuroCampus", color = Color.White) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        PantallaLista(
                            vm = postsViewModel,
                            onSeleccionarPost = { id ->
                                Log.d("MuroCampus", "Post seleccionado: $id")
                            }
                        )
                    }
                }
            }
        }
    }
}
