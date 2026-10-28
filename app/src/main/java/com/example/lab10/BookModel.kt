package com.example.lab10

import com.google.gson.annotations.SerializedName

// 1. El modelo principal de la respuesta
data class BookResponse(
    @SerializedName("items") val items: List<BookItem>?
)

// 2. Cada libro en la lista
data class BookItem(
    @SerializedName("id") val id: String,
    @SerializedName("volumeInfo") val volumeInfo: VolumeInfo
)

// 3. La información del libro (aquí están el título y la imagen)
data class VolumeInfo(
    @SerializedName("title") val title: String,
    @SerializedName("imageLinks") val imageLinks: ImageLinks?
)

// 4. Los links de las imágenes
data class ImageLinks(
    @SerializedName("thumbnail") val thumbnail: String?
)