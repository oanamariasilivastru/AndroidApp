package com.example.androidapp

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import com.example.androidapp.todo.data.remote.ItemService
import com.example.androidapp.auth.TAG
import com.example.androidapp.auth.data.AuthRepository
import com.example.androidapp.auth.data.remote.AuthDataSource
import com.example.androidapp.core.data.UserPreferencesRepository
import com.example.androidapp.core.data.remote.Api
import com.example.androidapp.core.ui.SyncWorker
import com.example.androidapp.todo.data.ItemRepository
import com.example.androidapp.todo.data.local.AppDatabase
import com.example.androidapp.todo.data.remote.ItemWsClient

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

class AppContainer(val context: Context) {
    init {
        Log.d(TAG, "init")
    }

    private val authDataSource: AuthDataSource = AuthDataSource()

    val itemService: ItemService = Api.retrofit.create(ItemService::class.java)
    val itemWsClient: ItemWsClient = ItemWsClient(Api.okHttpClient)

    // Migration from version 1 to 2
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE products ADD COLUMN description TEXT DEFAULT ''")
        }
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "product-database"
        )
            .addMigrations(MIGRATION_1_2)  // Apply migration
            .fallbackToDestructiveMigration()  // Optional for non-critical data
            .build()
    }

    val itemRepository: ItemRepository by lazy {
        ItemRepository(itemService, itemWsClient, database.productDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authDataSource)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.userPreferencesDataStore)
    }

}
