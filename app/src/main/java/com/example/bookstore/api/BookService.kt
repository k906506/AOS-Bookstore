package com.example.bookstore.api

import com.example.bookstore.model.BestSellerDTO
import com.example.bookstore.model.SearchBookDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookService {
    @GET("/api/search.api?output=json")
    fun getBooksByName(
        @Query("key") apiKey : String,
        @Query("query") keyword: String
    ) : Call<SearchBookDTO> // SearchBookDTO를 호출

    @GET("/api/bestSeller.api?output=json&categoryId=100")
    fun getBestSellerBooks(
        @Query("key") apiKey: String,
    ) : Call<BestSellerDTO> // BestSellerDTO를 호출
}