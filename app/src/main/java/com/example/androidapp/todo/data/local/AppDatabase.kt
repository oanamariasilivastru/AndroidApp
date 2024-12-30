package com.example.androidapp.todo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.androidapp.todo.data.Product

@Database(entities = arrayOf(Product::class), version = 3)
    abstract class AppDatabase: RoomDatabase() {
        abstract fun productDao(): ProductDao

        companion object {
            @Volatile
            private var INSTANCE: AppDatabase? = null

            fun getDatabase(context: Context): AppDatabase {
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "app_database")
                        .build()
                    INSTANCE = instance
                    instance
                }
            }
        }
}
