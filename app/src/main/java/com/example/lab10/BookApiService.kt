package com.example.lab10

import retrofit2.http.GET
import retrofit2.http.Query

interface BookApiService {
    @GET("books/v1/volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String = "AIzaSyAZjeeG85dN3qXEaF6Rs8Hq5G2ztrxo1Bc"
    ): BookResponse
}