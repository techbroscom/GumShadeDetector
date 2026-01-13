package com.techbros.myproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techbros.myproject.databinding.ItemTestBinding
import com.techbros.myproject.model.Test

class TestAdapter(
    private var tests: List<Test>,
    private val onItemClick: (Test) -> Unit // Click listener
) : RecyclerView.Adapter<TestAdapter.TestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val binding = ItemTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.bind(tests[position])
    }

    override fun getItemCount(): Int = tests.size

    fun updateTests(newTests: List<Test>) {
        tests = newTests
        notifyDataSetChanged()
    }

    class TestViewHolder(
        private val binding: ItemTestBinding,
        private val onItemClick: (Test) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: Test) {
            binding.test = test
            binding.executePendingBindings()

            // Set click listener
            binding.root.setOnClickListener {
                onItemClick(test)
            }
        }
    }
}
