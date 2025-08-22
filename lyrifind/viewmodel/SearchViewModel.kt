package com.example.lyrifind.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyrifind.data.model.Lyrics
import com.example.lyrifind.data.model.Song
import com.example.lyrifind.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repo: MusicRepository = MusicRepository()
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _lyrics = MutableStateFlow<Lyrics?>(null)
    val lyrics: StateFlow<Lyrics?> = _lyrics

    fun searchSongs(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = repo.searchSongs(query)
                _searchResults.value = results
            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadLyrics(song: Song) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repo.getLyrics(song)
                _lyrics.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                _lyrics.value = Lyrics(song.id, song.title, song.artist, "No lyrics found.")
            } finally {
                _isLoading.value = false
            }
        }
    }
}