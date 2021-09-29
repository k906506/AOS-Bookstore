package com.example.bookstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookstore.adapter.BookAdapter
import com.example.bookstore.api.BookService
import com.example.bookstore.databinding.ActivityMainBinding
import com.example.bookstore.model.BestSellerDTO
import com.example.bookstore.model.SearchBookDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BookAdapter
    private lateinit var bookService: BookService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBookRecyclerView()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bookService = retrofit.create(BookService::class.java)

        bookService.getBestSellerBooks(API_KEY)
            .enqueue(object : Callback<BestSellerDTO> {
                override fun onResponse(
                    call: Call<BestSellerDTO>,
                    response: Response<BestSellerDTO>
                ) {
                    if (response.isSuccessful.not()) {
                        // 예외 처리
                        return
                    }
                    response.body()?.let { // ?.로 null 처리
                        adapter.submitList(it.books)
                    }
                }

                override fun onFailure(call: Call<BestSellerDTO>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })

        binding.searchEditText.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == MotionEvent.ACTION_DOWN) {
                search(binding.searchEditText.text.toString())
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    private fun search(keyword: String) {
        bookService.getBooksByName(API_KEY, keyword).enqueue(object : Callback<SearchBookDTO> {
            override fun onResponse(
                call: Call<SearchBookDTO>,
                response: Response<SearchBookDTO>
            ) {
                if (response.isSuccessful.not()) {
                    // 예외 처리
                    return
                }
                adapter.submitList(response.body()?.books.orEmpty())
            }

            override fun onFailure(call: Call<SearchBookDTO>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun initBookRecyclerView() {
        adapter = BookAdapter()
        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = adapter
    }

    companion object {
        private const val BASE_URL = "https://book.interpark.com"
        private const val API_KEY =
            "FB949358DFB40DBBCE68FB95C3C870EAFC7AFEEB02D7B2D68345A88E36E541B6"
        private const val TAG = "MainActivity"
    }
}