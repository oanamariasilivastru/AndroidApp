package com.example.androidapp.todo.ui.items

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidapp.MyApplication
import com.example.androidapp.core.TAG
import com.example.androidapp.todo.data.ItemRepository
import com.example.androidapp.todo.data.Product
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ItemsViewModel(private val itemRepository: ItemRepository) : ViewModel() {

    // Transformarea fluxului pentru a emite doar `List<Product>`
    val uiState: Flow<List<Product>> = itemRepository.productStream
        .map { result ->
            when (result) {
                is com.example.androidapp.core.Result.Success -> result.data
                is com.example.androidapp.core.Result.Error -> {
                    Log.e(TAG, "Error loading products", result.exception)
                    emptyList() // Sau gestionează o stare de eroare
                }
                is com.example.androidapp.core.Result.Loading -> emptyList() // Sau o stare de încărcare
            }
        }
        .catch { e ->
            Log.e(TAG, "Exception in productStream", e)
            emit(emptyList()) // Emit o listă goală în caz de eroare
        }

    init {
        Log.d(TAG, "ItemsViewModel initialized")
        loadItems()
    }

    fun loadItems() {
        Log.d(TAG, "loadItems...")
        viewModelScope.launch {
            itemRepository.refresh()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                ItemsViewModel(app.container.itemRepository)
            }
        }
    }
}
