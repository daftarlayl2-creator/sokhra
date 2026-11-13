package com.sokhra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sokhra.databinding.ItemRowBinding

class ItemAdapter(
    private val items: MutableList<Item>,
    private val onItemClick: (Item) -> Unit,
    private val onItemLongClick: ((Item) -> Boolean)? = null
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(DiffCallback()) {

    private val visibleItems = mutableListOf<Item>()
    private val purchasedItems = mutableListOf<Item>()

    fun updateItems(newItems: List<Item>) {
        val current = currentList
        val merged = (current + newItems).distinctBy { it.id }
        val diff = diffMerge(current, merged)
        submitList(diff)
    }

    private fun diffMerge(current: List<Item>, new: List<Item>): List<Item> {
        val merged = mutableListOf<Item>()
        val currentMap = current.associateBy { it.id }
        val newMap = new.associateBy { it.id }

        for (id in currentMap.keys + newMap.keys) {
            val currentItem = currentMap[id]
            val newItem = newMap[id]
            if (newItem != null) {
                merged.add(newItem)
            } else if (currentItem != null) {
                merged.add(currentItem)
            }
        }
        return merged
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(private val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.tvName.text = item.name.ifEmpty { "اسم المنتج" }
            binding.tvCategory.text = getCategoryName(item.category)
            binding.tvQuantity.text = "${item.quantity} ${item.unit}"
            binding.tvPrice.text = if (item.price > 0) "السعر: ${"%.2f".format(item.price)} دج" else "أدخل سعراً"
            binding.checkBox.isChecked = item.purchased
            binding.checkBox.setOnCheckedChangeListener { _, _ ->
                val updated = item.copy(purchased = !item.purchased)
                val listManager = ShoppingListManager(binding.root.context)
                listManager.togglePurchased(item.id)
                val updatedItem = item.copy(purchased = !item.purchased)
                val index = items.indexOfFirst { it.id == updatedItem.id }
                if (index >= 0) {
                    items[index] = updatedItem
                }
                notifyItemChanged(index)
                binding.tvPrice.text = if (item.purchased) "مشترى ✅" else "غير مشتري"
            }
            binding.btnRemove.setOnClickListener {
                val listManager = ShoppingListManager(binding.root.context)
                listManager.togglePurchased(item.id)
                val updatedItem = item.copy(purchased = false)
                val index = items.indexOfFirst { it.id == updatedItem.id }
                if (index >= 0) {
                    items[index] = updatedItem
                }
                notifyItemChanged(index)
            }
            binding.root.setOnClickListener { onItemClick(item) }
            if (onItemLongClick != null) {
                binding.root.setOnLongClickListener { onItemLongClick?.invoke(item); true }
            }
        }

        private fun getCategoryName(category: String): String {
            return when (category) {
                "dairy" -> "ألبان"
                "meat" -> "لحوم"
                "vegetables" -> "خضراوات"
                "fruits" -> "فواكه"
                "bakery" -> "خبز"
                "beverages" -> "مشروبات"
                "household" -> "منزل"
                "snacks" -> "مأكولات خفيفة"
                "seafood" -> "مأكولات بحرية"
                "frozen" -> "مجمد"
                "pantry" -> "خزانة"
                "desserts" -> "حلويات"
                "grains" -> "حبوب"
                "spices" -> "توابل"
                "personal_care" -> "استخدام شخصي"
                "electronics" -> "إلكترونيات"
                "clothing" -> "ملابس"
                else -> category
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }
}
