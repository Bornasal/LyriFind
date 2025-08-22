package com.example.lyrifind.data.repository

import com.example.lyrifind.data.model.Lyrics
import com.example.lyrifind.data.model.Song
import com.example.lyrifind.data.remote.LyricsApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class MusicRepository {
    private val api = LyricsApiClient.apiService
    private val TAG = "MusicRepository"

    suspend fun searchSongs(query: String): List<Song> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Searching LRCLIB for: $query")

                val searchResults = api.searchLyrics(query)

                if (searchResults.isNotEmpty()) {
                    searchResults.take(10).mapIndexedNotNull { index, result ->
                        val trackName = result.trackName ?: result.name
                        val artistName = result.artistName

                        if (trackName != null && artistName != null) {
                            Song(
                                id = result.id?.toString() ?: "${index}_${trackName}_${artistName}",
                                title = trackName,
                                artist = artistName
                            )
                        } else null
                    }
                } else {
                    val parts = query.split(" by ", " - ", ignoreCase = true)

                    if (parts.size >= 2) {
                        val songTitle = parts[0].trim()
                        val artistName = parts[1].trim()

                        listOf(
                            Song(
                                id = "${artistName}_${songTitle}".replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), ""),
                                title = songTitle,
                                artist = artistName
                            )
                        )
                    } else {
                        listOf(
                            Song(
                                id = query.replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), ""),
                                title = query,
                                artist = "Unknown Artist"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Search error: ${e.message}", e)
                val parts = query.split(" by ", " - ", ignoreCase = true)

                if (parts.size >= 2) {
                    listOf(
                        Song(
                            id = "${parts[1]}_${parts[0]}".replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), ""),
                            title = parts[0].trim(),
                            artist = parts[1].trim()
                        )
                    )
                } else {
                    emptyList()
                }
            }
        }
    }

    suspend fun getLyrics(song: Song): Lyrics {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting lyrics for: '${song.title}' by '${song.artist}'")

                val cleanArtist = song.artist.replace("Unknown Artist", "").trim()
                val cleanTitle = song.title.trim()

                if (cleanArtist.isBlank() || cleanArtist == "Unknown Artist") {
                    return@withContext Lyrics(
                        songId = song.id,
                        title = song.title,
                        artist = song.artist,
                        lyrics = "Please search with format: 'Song Title by Artist Name'\n\nExample: 'Shape of You by Ed Sheeran'"
                    )
                }

                Log.d(TAG, "Calling LRCLIB API with artist: '$cleanArtist', title: '$cleanTitle'")

                val response = api.getLyrics(cleanArtist, cleanTitle)

                val lyricsText = when {
                    response.instrumental == true -> {
                        "This is an instrumental track (no lyrics available)"
                    }
                    !response.plainLyrics.isNullOrBlank() -> {
                        response.plainLyrics
                    }
                    !response.syncedLyrics.isNullOrBlank() -> {
                        // Remove timing info from synced lyrics for display
                        response.syncedLyrics.lines()
                            .map { line -> line.substringAfter("]").trim() }
                            .filter { it.isNotBlank() }
                            .joinToString("\n")
                    }
                    else -> {
                        "Lyrics not found for '$cleanTitle' by '$cleanArtist'.\n\n" +
                                "Tips:\n" +
                                "• Check spelling of song and artist names\n" +
                                "• Try searching for the song first\n" +
                                "• Some songs may not be in the database"
                    }
                }

                Lyrics(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    lyrics = lyricsText
                )

            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Timeout error: ${e.message}", e)
                Lyrics(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    lyrics = "Connection timeout. Please check your internet connection and try again."
                )
            } catch (e: java.net.UnknownHostException) {
                Log.e(TAG, "Network error: ${e.message}", e)
                Lyrics(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    lyrics = "Network error. Please check your internet connection."
                )
            } catch (e: Exception) {
                Log.e(TAG, "API error: ${e.message}", e)

                // Check if it's a 404 (not found) error
                if (e.message?.contains("404") == true) {
                    Lyrics(
                        songId = song.id,
                        title = song.title,
                        artist = song.artist,
                        lyrics = "Song not found in database.\n\n" +
                                "Try:\n" +
                                "• Different spelling\n" +
                                "• Search for the song first\n" +
                                "• Check if artist/song names are correct"
                    )
                } else {
                    Lyrics(
                        songId = song.id,
                        title = song.title,
                        artist = song.artist,
                        lyrics = "Error loading lyrics: ${e.message}\n\nPlease try again or check the song/artist spelling."
                    )
                }
            }
        }
    }
}