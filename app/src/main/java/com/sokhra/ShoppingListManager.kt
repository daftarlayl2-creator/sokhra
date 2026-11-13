package com.sokhra

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class ShoppingListManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sokhra_prefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Item>>() {}
    private val typeTokenList = object : TypeToken<List<ShoppingList>>() {}

    private val itemsKey = "items"
    private val listsKey = "lists"

    fun getItems(): List<Item> {
        val json = prefs.getString(itemsKey, "[]") ?: "[]"
        val listType = object : TypeToken<List<Item>>() {}.type
        return gson.fromJson(json, listType) ?: emptyList()
    }

    fun saveItems(items: List<Item>) {
        val json = gson.toJson(items)
        editor.putString(itemsKey, json).apply()
    }

    fun getItem(id: String): Item? {
        return getItems().find { it.id == id }
    }

    fun addItem(item: Item) {
        val items = getItems().toMutableList()
        items.add(item)
        saveItems(items)
    }

    fun updateItem(item: Item) {
        val items = getItems().toMutableList()
        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            items[index] = item
            saveItems(items)
        }
    }

    fun deleteItem(id: String) {
        val items = getItems().toMutableList()
        items.removeAll { it.id == id }
        saveItems(items)
    }

    fun togglePurchased(id: String): Item? {
        val items = getItems().toMutableList()
        val item = items.find { it.id == id } ?: return null
        val updated = item.copy(purchased = !item.purchased)
        items[items.indexOfFirst { it.id == id }] = updated
        saveItems(items)
        return updated
    }

    fun clearPurchased() {
        val items = getItems().toMutableList()
        items.removeAll { it.purchased }
        saveItems(items)
    }

    // Shopping Lists
    fun getLists(): List<ShoppingList> {
        val json = prefs.getString(listsKey, "[]") ?: "[]"
        return gson.fromJson(json, typeTokenList.type) ?: emptyList()
    }

    fun saveLists(lists: List<ShoppingList>) {
        val json = gson.toJson(lists)
        editor.putString(listsKey, json).apply()
    }

    fun addList(list: ShoppingList) {
        val lists = getLists().toMutableList()
        lists.add(list)
        saveLists(lists)
    }

    fun updateList(list: ShoppingList) {
        val lists = getLists().toMutableList()
        val index = lists.indexOfFirst { it.id == list.id }
        if (index >= 0) {
            lists[index] = list
            saveLists(lists)
        }
    }

    fun deleteList(id: String) {
        val lists = getLists().toMutableList()
        lists.removeAll { it.id == id }
        saveLists(lists)
    }

    fun getItemFromList(listId: String, itemId: String): Item? {
        val lists = getLists()
        val list = lists.find { it.id == listId } ?: return null
        return list.items.find { it.id == itemId }
    }

    fun saveItemsToList(listId: String, items: List<Item>) {
        val lists = getLists().toMutableList()
        val list = lists.find { it.id == listId } ?: return
        val updatedList = list.copy(items = items)
        lists[lists.indexOfFirst { it.id == listId }] = updatedList
        saveLists(lists)
    }

    fun duplicateList(listId: String): ShoppingList? {
        val lists = getLists()
        val original = lists.find { it.id == listId } ?: return null
        val now = System.currentTimeMillis()
        val duplicate = ShoppingList(
            id = java.util.UUID.randomUUID().toString(),
            name = "${original.name} (copy)",
            items = original.items,
            dateCreated = now
        )
        addList(duplicate)
        return duplicate
    }

    fun clearAllLists() {
        editor.remove(listsKey).apply()
    }
}

data class ShoppingList(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "قائمة جديدة",
    val items: List<Item> = emptyList(),
    val dateCreated: Long = System.currentTimeMillis(),
    val purchasedAt: Long = 0L
)

class DateUtils {
    companion object {
        fun formatDate(timestamp: Long): String {
            val date = Date(timestamp)
            val df = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            return df.format(date)
        }

        fun formatTime(timestamp: Long): String {
            val date = Date(timestamp)
            val df = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return df.format(date)
        }
    }
}
