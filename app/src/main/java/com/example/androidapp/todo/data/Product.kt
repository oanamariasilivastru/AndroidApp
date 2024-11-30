package com.example.androidapp.todo.data

data class Product(
    val _id: String? = null,
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val inStock: Boolean = false
)
