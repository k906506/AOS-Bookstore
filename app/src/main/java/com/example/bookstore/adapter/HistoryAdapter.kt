package com.example.bookstore.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.databinding.ItemHistoryBinding
import com.example.bookstore.model.History

class HistoryAdapter(val historyDeleteClickedListener: (String) -> Unit) :
    ListAdapter<History, HistoryAdapter.HistoryItemViewHolder>(diffUtil) {
    inner class HistoryItemViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(historyModel: History) {
            binding.historyKeywordTextView.text = historyModel.keyword
            binding.historyKeywordDeleteButton.setOnClickListener {
                historyDeleteClickedListener(historyModel.keyword.orEmpty())
            }
        }
    }

    // 뷰가 없을경우 뷰를 그려주는 메소드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        return HistoryItemViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context)))
    }

    // 뷰에 데이터를 넣어주는 bind 메소드
    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<History>() {
            override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem.keyword == newItem.keyword
            }

        }
    }
}