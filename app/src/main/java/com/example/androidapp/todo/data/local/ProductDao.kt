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

    @Update
    suspend fun update(product: Product)

    @Query("SELECT * FROM products WHERE isPendingSync = 1")
    suspend fun getPendingSyncProducts(): List<Product>

    @Query("UPDATE products SET isPendingSync = 0 WHERE _id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM products WHERE _id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
