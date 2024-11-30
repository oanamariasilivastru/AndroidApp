package com.example.androidapp


import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.example.androidapp.todo.data.remote.ItemService
import com.example.androidapp.auth.TAG
import com.example.androidapp.auth.data.AuthRepository
import com.example.androidapp.auth.data.remote.AuthDataSource
import com.example.androidapp.core.data.UserPreferencesRepository
import com.example.androidapp.core.data.remote.Api
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

    val database: AppDatabase by lazy {AppDatabase.getDatabase(context)}

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