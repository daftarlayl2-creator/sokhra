package com.sokhra

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sokhra.databinding.ListItemBinding

interface OnItemClickListener {
    fun onItemClick(list: ShoppingList)
}

class ShoppingListAdapter(
    private val lists: List<ShoppingList>,
    private val onItemClick: OnItemClickListener
) : RecyclerView.Adapter<ShoppingListAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val list = lists[position]
        holder.bind(list, onItemClick)
    }

    override fun getItemCount(): Int = lists.size

    inner class ListViewHolder(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(list: ShoppingList, onItemClick: OnItemClickListener) {
            binding.tvName.text = list.name
            binding.tvCategory.text = "قائمة"
            val items = list.items
            binding.tvQuantity.text = "${items.size} عنصر"
            binding.tvDate.text = DateUtils.formatDate(list.dateCreated)
            binding.tvPurchasedCount.text = "${items.count { it.purchased }} مشتري"

            binding.root.setOnClickListener {
                onItemClick.onItemClick(list)
            }

            binding.btnDelete.setOnClickListener {
                val listManager = ShoppingListManager(binding.root.context)
                listManager.deleteList(list.id)
                val newList = lists.toMutableList().apply { removeAt(adapterPosition) }
                notifyItemRemoved(adapterPosition)
                notifyItemRangeChanged(adapterPosition, newList.size)
            }
        }
    }
}
