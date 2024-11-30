package com.example.androidapp.todo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.androidapp.todo.data.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAll(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(products: List<Product>)

    @Update
    suspend fun update(product: Product): Int

    @Query("DELETE FROM products WHERE _id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
