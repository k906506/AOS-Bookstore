package com.example.bookstore.model

import com.google.gson.annotations.SerializedName

data class BestSellerDTO(
    @SerializedName("title") val id: String,
    @SerializedName("item") val books: List<Book>,
)
