// com/example/androidapp/todo/ui/items/ItemsViewModel.kt

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

    val uiState: Flow<List<Product>> = itemRepository.productStream

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
