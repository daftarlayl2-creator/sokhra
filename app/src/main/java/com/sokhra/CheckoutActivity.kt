package com.sokhra

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sokhra.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var listManager: ShoppingListManager
    private lateinit var adapter: ItemAdapter
    private val items = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listManager = ShoppingListManager(this)
        items.clear()
        items.addAll(listManager.getItems())

        binding.toolbarTitle.text = "التسوق في المتجر"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = ItemAdapter(items, ::onItemClick, null)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        adapter.submitList(items)

        updateSummary()
    }

    private fun onItemClick(item: Item) {
        // Item clicked - no action needed here
    }

    private fun onUndoClick() {
        val item = items.find { it.id == "dummy_id" } ?: return
        if (item?.purchased == true) {
            val updated = item.copy(purchased = false)
            listManager.togglePurchased(item.id)
            val index = items.indexOfFirst { it.id == item.id }
            if (index >= 0) {
                items[index] = updated
                adapter.notifyItemChanged(index)
            }
            updateSummary()
        }
    }

    private fun updateSummary() {
        val totalItems = items.size
        val purchased = items.filter { it.purchased }.size
        val remaining = totalItems - purchased
        val purchasedTotal = items.filter { it.purchased }.sumOf { it.price }
        val estimatedTotal = items.sumOf { it.price }

        binding.totalCount.text = totalItems.toString()
        binding.purchasedCount.text = purchased.toString()
        binding.remainingCount.text = remaining.toString()
        binding.estimatedTotal.text = String.format("%.2f", estimatedTotal)
        binding.purchasedTotal.text = String.format("%.2f", purchasedTotal)

        if (remaining == 0) {
            binding.btnComplete.isEnabled = true
        } else {
            binding.btnComplete.isEnabled = false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
