package com.example.bookstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.bookstore.api.BookService
import com.example.bookstore.model.BestSellerDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val bookService = retrofit.create(BookService::class.java)

        bookService.getBestSellerBooks(API_KEY)
            .enqueue(object : Callback<BestSellerDTO> {
                override fun onResponse(
                    call: Call<BestSellerDTO>,
                    response: Response<BestSellerDTO>
                ) {
                    if (response.isSuccessful.not()) {
                        // 예외 처리
                    }
                    response.body()?.let { // ?.로 null 처리
                        Log.d(TAG, it.toString())
                        it.books.forEach {
                            book -> Log.d(TAG, book.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<BestSellerDTO>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
    }

    companion object {
        private const val BASE_URL = "http://book.interpark.com"
        private const val API_KEY = "FB949358DFB40DBBCE68FB95C3C870EAFC7AFEEB02D7B2D68345A88E36E541B6"
        private const val TAG = "MainActivity"
    }
}