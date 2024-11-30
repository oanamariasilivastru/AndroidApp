package com.example.androidapp.todo.data.remote

import com.example.androidapp.todo.data.Product
import retrofit2.http.*

interface ItemService {
    @GET("/api/product") // Ruta pentru a obține toate produsele
    suspend fun find(@Header("Authorization") authorization: String): List<Product>

    @GET("/api/product/{id}") // Ruta pentru a obține un produs specific după ID
    suspend fun read(
        @Header("Authorization") authorization: String,
        @Path("id") itemId: String?
    ): Product

    @Headers("Content-Type: application/json")
    @POST("/api/product/{id}") // Ruta pentru a crea un produs nou
    suspend fun create(
        @Header("Authorization") authorization: String,
        @Body product: Product
    ): Product

    @Headers("Content-Type: application/json")
    @PUT("/api/product/{id}") // Ruta pentru a actualiza un produs existent
    suspend fun update(
        @Header("Authorization") authorization: String,
        @Path("id") itemId: String?,
        @Body product: Product
    ): Product

    @DELETE("/{id}") // Ruta pentru a șterge un produs
    suspend fun delete(
        @Header("Authorization") authorization: String,
        @Path("id") itemId: String?
    )
}
