package com.sokhra

data class Item(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val category: String = "grains",
    val quantity: String = "1",
    val unit: String = "piece",
    val price: Double = 0.0,
    val note: String = "",
    val purchased: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)
