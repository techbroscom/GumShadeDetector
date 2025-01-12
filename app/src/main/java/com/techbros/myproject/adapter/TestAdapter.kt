package com.techbros.myproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techbros.myproject.databinding.ItemTestBinding
import com.techbros.myproject.model.Test

class TestAdapter(private val tests: List<Test>) : RecyclerView.Adapter<TestAdapter.TestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val binding = ItemTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.bind(tests[position])
    }

    override fun getItemCount(): Int = tests.size

    class TestViewHolder(private val binding: ItemTestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(test: Test) {
            binding.test = test
            binding.executePendingBindings()
        }
    }
}
