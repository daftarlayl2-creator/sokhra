package com.sokhra

import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sokhra.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var listManager: ShoppingListManager
    private lateinit var adapter: ItemAdapter
    private val items = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize managers
        listManager = ShoppingListManager(this)

        // Load saved items
        items.clear()
        items.addAll(listManager.getItems())

        // Setup toolbar
        setSupportActionBar(binding.toolbar)

        // Setup bottom nav
        setupBottomNav()

        // Setup UI buttons
        binding.btnNewList.setOnClickListener { showNewListDialog() }
        binding.btnAddProduct.setOnClickListener { showAddProductDialog() }
        binding.btnCheckout.setOnClickListener { showCheckoutDialog() }
        binding.btnHistory.setOnClickListener { navigateToHistory() }

        // Setup RecyclerView
        adapter = ItemAdapter(items, ::onItemClick, ::onItemLongClick)
        adapter.submitList(items)
        Toast.makeText(this, "قائمة التسوق", Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNav() {
        Toast.makeText(this, "القائمة الرئيسية", Toast.LENGTH_SHORT).show()
    }

    private fun showAddProductDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(R.string.add_product)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = dialogView.findViewById<TextInputEditText>(R.id.etName).text.toString().trim()
                val category = dialogView.findViewById<TextInputEditText>(R.id.etCategory).text.toString().trim()
                val quantity = dialogView.findViewById<TextInputEditText>(R.id.etQuantity).text.toString().trim()
                val unit = dialogView.findViewById<TextInputEditText>(R.id.etUnit).text.toString().trim()
                val priceStr = dialogView.findViewById<TextInputEditText>(R.id.etPrice).text.toString().trim()
                val note = dialogView.findViewById<TextInputEditText>(R.id.etNote).text.toString().trim()

                if (name.isBlank()) { Toast.makeText(this, "يرجى إدخال اسم المنتج", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                if (quantity.isBlank()) { Toast.makeText(this, "يرجى إدخال الكمية", Toast.LENGTH_SHORT).show(); return@setPositiveButton }

                val price = priceStr.toDoubleOrNull() ?: 0.0

                val item = Item(
                    name = name,
                    category = category.ifBlank { "grains" },
                    quantity = quantity,
                    unit = unit.ifBlank { "piece" },
                    price = price,
                    note = note
                )
                listManager.addItem(item)
                items.add(item)
                adapter.submitList(items)
                Toast.makeText(this, "تم إضافة المنتج", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .create().show()
    }

    private fun showCheckoutDialog() {
        val purchased = items.filter { it.purchased }
        val remaining = items.filter { !it.purchased }

        val total = items.sumOf { it.price }
        val purchasedTotal = purchased.sumOf { it.price }
        val remainingCount = remaining.size
        val purchasedCount = purchased.size
        val progress = if (items.isNotEmpty()) (purchasedCount * 100 / items.size) else 0

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_checkout, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(R.string.checkout)
            .setView(dialogView)
            .setPositiveButton(R.string.checkout) { _, _ ->
                // All done - show confirmation
                val confirm = android.app.AlertDialog.Builder(this)
                    .setTitle(R.string.all_purchased)
                    .setMessage(R.string.you_bought_all_items)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        listManager.clearPurchased()
                        items.removeAll { it.purchased }
                        adapter.notifyDataSetChanged()
                        Toast.makeText(this, "تمت العملية بنجاح", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(R.string.no, null)
                    .create()
                confirm.show()
            }
            .setNegativeButton(R.string.cancel, null)
            .create().show()
    }

    private fun showNewListDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_list, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(R.string.new_list)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = dialogView.findViewById<TextInputEditText>(R.id.etListName).text.toString().trim()
                if (name.isBlank()) { Toast.makeText(this, "يرجى إدخال اسم القائمة", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                val list = ShoppingList(name = name)
                listManager.addList(list)
                Toast.makeText(this, "تم إنشاء القائمة", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .create().show()
    }

    private fun navigateToHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    private fun onItemClick(item: Item) {
        // Open item detail or edit
        Toast.makeText(this, "تفاعل مع: ${item.name}", Toast.LENGTH_SHORT).show()
    }

    private fun onItemLongClick(item: Item): Boolean {
        showItemOptions(item)
        return true
    }

    private fun showItemOptions(item: Item) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(R.string.actions)
            .setItems(arrayOf(resources.getString(R.string.edit), resources.getString(R.string.delete)), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    when (which) {
                        0 -> showEditDialog(item)
                        1 -> { listManager.deleteItem(item.id); items.remove(item); adapter.notifyItemRemoved(items.size); }
                    }
                }
            })
            .create()
        dialog.show()
    }

    private fun showEditDialog(item: Item) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(R.string.edit)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = dialogView.findViewById<TextInputEditText>(R.id.etName).text.toString().trim()
                val category = dialogView.findViewById<TextInputEditText>(R.id.etCategory).text.toString().trim()
                val quantity = dialogView.findViewById<TextInputEditText>(R.id.etQuantity).text.toString().trim()
                val unit = dialogView.findViewById<TextInputEditText>(R.id.etUnit).text.toString().trim()
                val priceStr = dialogView.findViewById<TextInputEditText>(R.id.etPrice).text.toString().trim()
                val note = dialogView.findViewById<TextInputEditText>(R.id.etNote).text.toString().trim()

                if (name.isBlank()) { Toast.makeText(this, "يرجى إدخال اسم المنتج", Toast.LENGTH_SHORT).show(); return@setPositiveButton }

                val price = priceStr.toDoubleOrNull() ?: 0.0

                val updated = item.copy(
                    name = name,
                    category = category.ifBlank { item.category },
                    quantity = quantity,
                    unit = unit.ifBlank { item.unit },
                    price = price,
                    note = note
                )
                listManager.updateItem(updated)
                items[items.indexOfFirst { it.id == item.id }] = updated
                adapter.notifyItemChanged(items.size)
                Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .create().show()
    }

    override fun onResume() {
        super.onResume()
        items.clear()
        items.addAll(listManager.getItems())
        adapter.submitList(items)
    }
}
