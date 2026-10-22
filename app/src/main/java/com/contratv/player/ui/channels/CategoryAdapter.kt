package com.contratv.player.ui.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.contratv.player.api.models.Category
import com.contratv.player.databinding.ItemCategoryBinding

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedIndex = 0

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, position == selectedIndex)
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, isSelected: Boolean) {
            binding.tvCategoryName.text = category.categoryName
            binding.tvCategoryCount.text = ""
            binding.root.isSelected = isSelected

            binding.root.setOnClickListener {
                val oldIndex = selectedIndex
                selectedIndex = adapterPosition
                notifyItemChanged(oldIndex)
                notifyItemChanged(selectedIndex)
                onCategoryClick(category)
            }

            // Also support D-pad navigation selection on TV
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.root.isSelected = true
                }
            }
        }
    }
}
