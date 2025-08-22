package com.example.lyrifind.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface LyricsApiService {
    @GET("get")
    suspend fun getLyrics(
        @Query("artist_name") artist: String,
        @Query("track_name") track: String
    ): LRCLibResponse

    @GET("search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LRCLibResponse>
}