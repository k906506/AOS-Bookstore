package com.example.bookstore.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class Review(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "review") val review: String?
)