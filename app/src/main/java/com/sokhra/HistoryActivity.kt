package com.sokhra

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.sokhra.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var listManager: ShoppingListManager
    private lateinit var adapter: ShoppingListAdapter
    private val lists = mutableListOf<ShoppingList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listManager = ShoppingListManager(this)
        lists.clear()
        lists.addAll(listManager.getLists())

        binding.toolbarTitle.text = "سجل التسوق"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = ShoppingListAdapter(lists, object : OnItemClickListener {
            override fun onItemClick(list: ShoppingList) {
                openList(list)
            }
        })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.btnNewList.setOnClickListener { showNewListDialog() }
        binding.btnRestore.setOnClickListener { showRestoreDialog() }

        if (lists.isEmpty()) {
            binding.emptyList.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyList.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun onListItemClick(list: ShoppingList) {
        (this@HistoryActivity as OnItemClickListener).onItemClick(list)
    }

    private fun openList(list: ShoppingList) {
        val intent = Intent(this, ShoppingActivity::class.java).apply {
            putExtra("list_id", list.id)
            putExtra("list_name", list.name)
        }
        startActivity(intent)
    }

    private fun showNewListDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_list, null)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.new_list)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = dialogView.findViewById<TextInputEditText>(R.id.etListName).text.toString().trim()
                if (name.isBlank()) { Toast.makeText(this, "يرجى إدخال اسم القائمة", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                val list = ShoppingList(name = name)
                listManager.addList(list)
                lists.add(list)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "تم إنشاء القائمة", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .create().show()
    }

    private fun showRestoreDialog() {
        if (lists.isEmpty()) {
            Toast.makeText(this, "لا توجد قوائم لحذفها", Toast.LENGTH_SHORT).show()
            return
        }
        val items = lists.map { "${it.name} - ${it.items.size} عناصر" }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.restore_list)
            .setItems(items) { _, which ->
                val selected = lists[which]
                listManager.duplicateList(selected.id)?.let { newList ->
                    Toast.makeText(this, "تم استعادة القائمة: ${newList.name}", Toast.LENGTH_SHORT).show()
                    lists.add(newList)
                    adapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create().show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
