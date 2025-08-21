package com.example.lyrifind.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyrifind.data.model.Lyrics
import com.example.lyrifind.data.model.Song
import com.example.lyrifind.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LyricsViewModel(
    private val repo: MusicRepository = MusicRepository()
) : ViewModel() {

    private val _lyrics = MutableStateFlow<Lyrics?>(null)
    val lyrics: StateFlow<Lyrics?> = _lyrics

    fun loadLyrics(song: Song) {
        viewModelScope.launch {
            try {
                val result = repo.getLyrics(song)
                _lyrics.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                _lyrics.value = Lyrics(song.id, song.title, song.artist, "No lyrics found.")
            }
        }
    }
}
