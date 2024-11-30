package com.example.androidapp.todo.data.remote


import com.example.androidapp.todo.data.Product

data class Payload(val updateProduct: Product)
data class ItemEvent(val type: String, val payload: Product)
