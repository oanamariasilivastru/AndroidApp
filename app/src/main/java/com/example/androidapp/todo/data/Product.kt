package com.example.androidapp.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val _id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val inStock: Boolean = false
)
