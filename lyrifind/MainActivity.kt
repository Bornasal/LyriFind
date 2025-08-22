package com.example.lyrifind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lyrifind.data.model.Song
import com.example.lyrifind.ui.screens.LyricsScreen
import com.example.lyrifind.ui.screens.SearchScreen
import com.example.lyrifind.viewmodel.SearchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AppNavHost(navController)
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    var selectedSong by remember { mutableStateOf<Song?>(null) }

    val searchViewModel: SearchViewModel = viewModel()

    var persistentSearchQuery by remember { mutableStateOf("") }

    NavHost(navController, startDestination = "search") {
        composable("search") {
            SearchScreen(
                onSongClick = { song ->
                    selectedSong = song
                    navController.navigate("lyrics")
                },
                viewModel = searchViewModel,
                persistentSearchQuery = persistentSearchQuery,
                onSearchQueryChange = { query ->
                    persistentSearchQuery = query
                }
            )
        }
        composable("lyrics") {
            selectedSong?.let { song ->
                LyricsScreen(
                    song = song,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}