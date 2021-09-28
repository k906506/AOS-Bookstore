package com.example.bookstore.model

import com.google.gson.annotations.SerializedName

data class SearchBookDTO(
    @SerializedName("title") val id: String,
    @SerializedName("item") val books: List<Book>,
)