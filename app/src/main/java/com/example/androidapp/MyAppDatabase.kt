package com.example.androidapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.androidapp.todo.data.Product
//import com.example.myapp.todo.data.local.ItemDao

//@Database(entities = arrayOf(Product::class), version = 2)
//abstract class MyAppDatabase : RoomDatabase() {
//    abstract fun itemDao(): ItemDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: MyAppDatabase? = null
//
//        fun getDatabase(context: Context): MyAppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context,
//                    MyAppDatabase::class.java,
//                    "app_database"
//                )
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}
